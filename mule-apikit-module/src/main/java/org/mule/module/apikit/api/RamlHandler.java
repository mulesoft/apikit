/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api;

import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.StreamUtils;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.parser.ParserService;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.api.exception.TypedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.mule.runtime.core.api.MuleContext;
import org.raml.model.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RamlHandler {

  public static final String APPLICATION_RAML = "application/raml+yaml";
  private static final String RAML_QUERY_STRING = "raml";

  private boolean keepRamlBaseUri;
  private String apiServer;
  private IRaml api;
  private ParserService parserService;

  private String apiResourcesRelativePath = "";

  protected static final Logger logger = LoggerFactory.getLogger(RamlHandler.class);


  private MuleContext muleContext;

  //ramlLocation should be the root raml location, relative of the resources folder
  public RamlHandler(String ramlLocation, boolean keepRamlBaseUri, MuleContext muleContext) throws IOException {
    this.keepRamlBaseUri = keepRamlBaseUri;

    String rootRamlLocation = findRootRaml(ramlLocation);
    if (rootRamlLocation == null) {
      throw new IOException("Raml not found at: " + ramlLocation);
    }
    parserService = new ParserService(rootRamlLocation);
    parserService.validateRaml();
    this.api = parserService.build();

    int idx = rootRamlLocation.lastIndexOf("/");
    if (idx > 0) {
      this.apiResourcesRelativePath = rootRamlLocation.substring(0, idx + 1);
      this.apiResourcesRelativePath = sanitarizeResourceRelativePath(apiResourcesRelativePath);
    }
    this.muleContext = muleContext;
  }

  public boolean isParserV2() {
    return parserService.isParserV2();
  }

  public IRaml getApi() {
    return api;
  }

  public void setApi(IRaml api) {
    this.api = api;
  }

  public String getRamlV1() {
    if (keepRamlBaseUri) {
      return parserService.dumpRaml(api);
    } else {
      String baseUriReplacement = getBaseUriReplacement(apiServer);
      return parserService.dumpRaml(api, baseUriReplacement);
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
      String rootRaml = parserService.dumpRaml(api);
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

  public String getBaseUriReplacement(String apiServer) {
    return UrlUtils.getBaseUriReplacement(apiServer);
  }

  public boolean isRequestingRamlV1ForConsole(String listenerPath, String requestPath, String queryString, String method,
                                              String acceptHeader) {
    String postalistenerPath = UrlUtils.getListenerPath(listenerPath, requestPath);

    return (!isParserV2() &&
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
    return isParserV2() && queryString.equals(RAML_QUERY_STRING)
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
      String[] startingLocations = new String[] {"", "api/", "api"};
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
