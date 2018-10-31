/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.mule.amf.impl.ParserWrapperAmf;
import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.StreamUtils;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.parser.service.ParserService;
import org.mule.raml.interfaces.ParserType;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.model.ApiRef;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.api.exception.TypedException;
import org.mule.runtime.core.api.MuleContext;
import org.raml.model.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mule.apikit.common.APISyncUtils.isSyncProtocol;
import static org.mule.raml.interfaces.ParserType.AMF;
import static org.mule.raml.interfaces.ParserType.AUTO;
import static org.mule.raml.interfaces.ParserType.RAML;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_08;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_10;

public class RamlHandler {

  public static final String APPLICATION_RAML = "application/raml+yaml";
  private static final String RAML_QUERY_STRING = "raml";

  private boolean keepRamlBaseUri;
  private String apiServer;
  private IRaml api;
  private ParserWrapper parserWrapper;

  private String apiResourcesRelativePath = "";

  private ParserType parser;

  protected static final Logger logger = LoggerFactory.getLogger(RamlHandler.class);

  public static final String MULE_APIKIT_PARSER_AMF = "mule.apikit.parser";

  private MuleContext muleContext;

  //ramlLocation should be the root raml location, relative of the resources folder
  public RamlHandler(String ramlLocation, boolean keepRamlBaseUri, MuleContext muleContext) throws IOException {
    this(ramlLocation, keepRamlBaseUri, muleContext, null);
  }

  public RamlHandler(String ramlLocation, boolean keepRamlBaseUri, MuleContext muleContext, ParserType defaultParser)
      throws IOException {
    this.keepRamlBaseUri = keepRamlBaseUri;

    parser = resolveParser(defaultParser);

    String rootRamlLocation = findRootRaml(ramlLocation);
    if (rootRamlLocation == null) {
      throw new IOException("Raml not found at: " + ramlLocation);
    }
    parserWrapper = ParserService.create(ApiRef.create(rootRamlLocation), parser);
    parserWrapper.validate();
    this.api = parserWrapper.build();
    parser = parserWrapper.getParserType(); // Fix Parser 

    int idx = rootRamlLocation.lastIndexOf("/");
    if (idx > 0) {
      this.apiResourcesRelativePath = rootRamlLocation.substring(0, idx + 1);
      this.apiResourcesRelativePath = sanitarizeResourceRelativePath(apiResourcesRelativePath);
    } else if (isSyncProtocol(apiResourcesRelativePath)) {
      this.apiResourcesRelativePath = rootRamlLocation;
    }

    this.muleContext = muleContext;
  }

  private ParserType resolveParser(ParserType defaultValue) {
    final String parserValue = System.getProperty(MULE_APIKIT_PARSER_AMF);
    if (AMF.name().equals(parserValue))
      return AMF;
    if (RAML.name().equals(parserValue))
      return RAML;
    return defaultValue == null ? AUTO : defaultValue;
  }

  public ParserType getParserType() {
    return parser;
  }

  /**
   * @deprecated use getApiVendor() instead.
   */
  @Deprecated
  public boolean isParserV2() {
    final ParserType parser = getParserType();
    return parser == AMF || (parser == RAML && ApiVendor.RAML_10 == getApiVendor());
  }

  public ApiVendor getApiVendor() {
    return parserWrapper.getApiVendor();
  }

  public IRaml getApi() {
    return api;
  }

  public void setApi(IRaml api) {
    this.api = api;
  }

  public String getRamlV1() {
    if (keepRamlBaseUri) {
      return parserWrapper.dump(api, null);
    } else {
      String baseUriReplacement = getBaseUriReplacement(apiServer);
      return parserWrapper.dump(api, baseUriReplacement);
    }
  }


  //resourcesRelativePath should not contain the console path
  public String getRamlV2(String resourceRelativePath) throws TypedException {
    resourceRelativePath = sanitarizeResourceRelativePath(resourceRelativePath);
    if (resourceRelativePath.contains("..")) {
      throw ApikitErrorTypes.throwErrorType(new NotFoundException("\"..\" is not allowed"));
    }
    if (apiResourcesRelativePath.equals(resourceRelativePath)) {
      //root raml
      String rootRaml = parserWrapper.dump(api, null);
      if (keepRamlBaseUri) {
        return rootRaml;
      }
      String baseUriReplacement = getBaseUriReplacement(apiServer);
      return UrlUtils.replaceBaseUri(rootRaml, baseUriReplacement);
    } else {
      //the resource should be in a subfolder, otherwise it could be requesting the properties file
      if (!resourceRelativePath.contains("/")) {
        throw ApikitErrorTypes.throwErrorType(new NotFoundException("Requested resources should be in a subfolder"));
      }
      //resource
      InputStream apiResource = null;
      ByteArrayOutputStream baos = null;
      try {
        apiResource = muleContext.getExecutionClassLoader().getResourceAsStream(resourceRelativePath);

        if (apiResource == null && resourceRelativePath.startsWith(apiResourcesRelativePath)) {
          apiResource = parserWrapper.fetchResource(resourceRelativePath.substring(apiResourcesRelativePath.length()));
        }

        if (apiResource == null) {
          throw ApikitErrorTypes.throwErrorType(new NotFoundException(resourceRelativePath));
        }

        baos = new ByteArrayOutputStream();
        StreamUtils.copyLarge(apiResource, baos);
      } catch (IOException e) {
        logger.debug(e.getMessage());
        throw ApikitErrorTypes.throwErrorType(new NotFoundException(resourceRelativePath));
      } finally {
        IOUtils.closeQuietly(apiResource);
        IOUtils.closeQuietly(baos);
      }
      if (baos != null) {
        return baos.toString();
      }
      return null;
    }
  }

  public String getAMFModel() {
    return (parserWrapper instanceof ParserWrapperAmf) ? ((ParserWrapperAmf) parserWrapper).getAmfModel() : "";
  }

  public String getBaseUriReplacement(String apiServer) {
    return UrlUtils.getBaseUriReplacement(apiServer);
  }

  public boolean isRequestingRamlV1ForConsole(String listenerPath, String requestPath, String queryString, String method,
                                              String acceptHeader) {
    String postalistenerPath = UrlUtils.getListenerPath(listenerPath, requestPath);

    return (getApiVendor().equals(RAML_08) &&
        (postalistenerPath.equals(requestPath) || (postalistenerPath + "/").equals(requestPath)) &&
        ActionType.GET.toString().equals(method.toUpperCase()) &&
        (APPLICATION_RAML.equals(acceptHeader)
            || queryString.equals(RAML_QUERY_STRING)));
  }

  public boolean isRequestingRamlV2(String listenerPath, String requestPath, String queryString, String method) {
    String consolePath = UrlUtils.getListenerPath(listenerPath, requestPath);
    String resourcesFullPath = consolePath;
    if (!consolePath.endsWith("/")) {
      if (!apiResourcesRelativePath.startsWith("/")) {
        resourcesFullPath += "/";
      }
      resourcesFullPath += apiResourcesRelativePath;
    } else {
      if (apiResourcesRelativePath.startsWith("/") && apiResourcesRelativePath.length() > 1) {
        resourcesFullPath += apiResourcesRelativePath.substring(1);
      }
    }
    return getApiVendor().equals(RAML_10) && queryString.equals(RAML_QUERY_STRING)
        && ActionType.GET.toString().equals(method.toUpperCase())
        && requestPath.startsWith(resourcesFullPath);
  }

  private String sanitarizeResourceRelativePath(String resourceRelativePath) {
    //delete first slash
    if (resourceRelativePath.startsWith("/") && resourceRelativePath.length() > 1) {
      resourceRelativePath = resourceRelativePath.substring(1, resourceRelativePath.length());
    }
    //delete querystring
    if (resourceRelativePath.contains("?raml")) {
      resourceRelativePath = resourceRelativePath.substring(0, resourceRelativePath.indexOf('?'));
    }
    //delete last slash
    if (resourceRelativePath.endsWith("/") && resourceRelativePath.length() > 1) {
      resourceRelativePath = resourceRelativePath.substring(0, resourceRelativePath.length() - 1);
    }
    return resourceRelativePath;
  }

  private String findRootRaml(String ramlLocation) {
    try {
      final URL url = new URL(ramlLocation);
      return url.toString();
    } catch (MalformedURLException e) {
      String[] startingLocations = new String[] {"api/", "", "api"};
      for (String start : startingLocations) {
        URL ramlLocationUrl = Thread.currentThread().getContextClassLoader().getResource(start + ramlLocation);
        if (ramlLocationUrl != null) {
          return start + ramlLocation;
        }
      }
    }
    return null;
  }

  public String getRootRamlLocationForV2() {
    return "this.location.href" + " + '" + apiResourcesRelativePath + "/?" + RAML_QUERY_STRING + "'";
  }

  public String getRootRamlLocationForV1() {
    return "this.location.href" + " + '" + "?" + RAML_QUERY_STRING + "'";
  }

  public String getSuccessStatusCode(IAction action) {

    for (String status : action.getResponses().keySet()) {
      int code = Integer.parseInt(status);
      if (code >= 200 && code < 300) {
        return status;
      }
    }
    //default success status
    return "200";
  }

  public void setApiServer(String apiServer) {
    this.apiServer = apiServer;
  }
}
