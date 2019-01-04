/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.apache.maven.plugin.logging.Log;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.logger.Logger;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.api.ApiRef;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.model.ScaffolderReport;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;
import org.mule.tools.apikit.output.GenerationModel;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class RAMLFilesParser {

  private Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = new HashMap<>();
  private final APIFactory apiFactory;
  private final Log log;

  private String ramlVersion;
  private String parseStatus = ScaffolderReport.SUCCESS;
  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getVendorId() {
    return vendorId;
  }

  public String getRamlVersion() {
    return ramlVersion;
  }

  public String getParseStatus() {
    return parseStatus;
  }

  private static String vendorId = "RAML";

  public static final String MULE_APIKIT_PARSER = "mule.apikit.parser";


  private RAMLFilesParser(Log log, List<ApiRef> specs, APIFactory apiFactory, ScaffolderResourceLoader scaffolderResourceLoader) {
    this.log = log;
    this.apiFactory = apiFactory;
    List<ApiRef> processedFiles = new ArrayList<>();
    for (ApiRef spec : specs) {
      try {
        final ParserWrapper parserWrapper = getParserWrapper(spec, scaffolderResourceLoader);
        parserWrapper.validate(); // This will fail whether the raml is not valid

        vendorId = parserWrapper.getApiVendor().toString();
        final IRaml raml = parserWrapper.build();
        ramlVersion = raml.getVersion();

        collectResources(spec.getLocation(), raml.getResources(), API.DEFAULT_BASE_URI, raml.getVersion());
        processedFiles.add(spec);
      } catch (Exception e) {
        final String reason = e.getMessage() == null ? "" : " Reason: " + e.getMessage();
        log.info("Could not parse [" + spec.getLocation() + "] as root RAML file." + reason);
        log.debug(e);
      }

    }
    if (processedFiles.size() > 0) {
      this.log.info("The following RAML files were parsed correctly: " +
          processedFiles);
    } else {
      parseStatus = ScaffolderReport.FAILED;
      errorMessage = "None of the files was recognized as a valid root API file. See the Error Log for more details";
      this.log.error(errorMessage);
    }
  }

  public static RAMLFilesParser create(Log log, Map<File, InputStream> fileStreams, APIFactory apiFactory) {
    final List<ApiRef> specs = fileStreams.entrySet().stream()
        .map(e -> ApiRef.create(e.getKey().getAbsolutePath()))
        .collect(toList());

    return new RAMLFilesParser(log, specs, apiFactory, null);
  }

  public static RAMLFilesParser create(Log log, Map<String, InputStream> apis, APIFactory apiFactory,
                                       ScaffolderResourceLoader scaffolderResourceLoader) {
    final List<ApiRef> specs = apis.entrySet().stream()
        .map(e -> ApiRef.create(e.getKey()))
        .collect(toList());

    return new RAMLFilesParser(log, specs, apiFactory, scaffolderResourceLoader);
  }


  private void collectResources(String filePath, Map<String, IResource> resourceMap, String baseUri, String version) {
    for (IResource resource : resourceMap.values()) {
      for (IAction action : resource.getActions().values()) {


        API api = apiFactory.createAPIBinding(filePath, null, baseUri, APIKitTools.getPathFromUri(baseUri, false), null,
                                              null);

        Map<String, IMimeType> mimeTypes = action.getBody();
        boolean addGenericAction = false;
        if (mimeTypes != null && !mimeTypes.isEmpty()) {
          for (IMimeType mimeType : mimeTypes.values()) {
            if (mimeType.getSchema() != null
                || (mimeType.getFormParameters() != null && !mimeType.getFormParameters().isEmpty())) {
              addResource(api, resource, action, mimeType.getType(), version);
            } else {
              addGenericAction = true;
            }
          }
        } else {
          addGenericAction = true;
        }

        if (addGenericAction) {
          addResource(api, resource, action, null, version);
        }
      }

      collectResources(filePath, resource.getResources(), baseUri, version);
    }
  }

  private void addResource(API api, IResource resource, IAction action, String mimeType, String version) {

    String completePath = APIKitTools
        .getCompletePathFromBasePathAndPath(api.getHttpListenerConfig().getBasePath(), api.getPath());

    ResourceActionMimeTypeTriplet resourceActionTriplet =
        new ResourceActionMimeTypeTriplet(api, completePath + resource.getResolvedUri(version),
                                          action.getType().toString(),
                                          mimeType);
    entries.put(resourceActionTriplet, new GenerationModel(api, version, resource, action, mimeType));
  }

  public Map<ResourceActionMimeTypeTriplet, GenerationModel> getEntries() {
    return entries;
  }

  private ParserWrapper getParserWrapper(ApiRef apiRef, ScaffolderResourceLoader scaffolderResourceLoader) {
    if (scaffolderResourceLoader == null) {
      apiRef = ApiRef.create(apiRef.getLocation());
    } else {
      apiRef = ApiRef.create(apiRef.getLocation(), scaffolderResourceLoader);
    }

    final ParserService parserService = new ParserService(LoggerWrapper.getLogger(log));
    final ParserWrapper parser = parserService.getParser(apiRef);
    log.info(buildParserInfoMessage(parser.getParserType().name()));
    return parser;
  }


  private String buildParserInfoMessage(String parser) {
    return format("Using %s to load APIs", parser);
  }

  private static class LoggerWrapper implements Logger {

    private final Log logger;

    private LoggerWrapper(Log logger) {
      this.logger = logger;
    }

    static LoggerWrapper getLogger(Log logger) {
      return new LoggerWrapper(logger);
    }

    @Override
    public void debug(String msg) {
      logger.debug(msg);
    }

    @Override
    public void debug(String msg, Throwable error) {
      logger.debug(msg, error);
    }

    @Override
    public void info(String msg) {
      logger.info(msg);
    }

    @Override
    public void info(String msg, Throwable error) {
      logger.info(msg, error);
    }

    @Override
    public void warn(String msg) {
      logger.warn(msg);
    }

    @Override
    public void warn(String msg, Throwable error) {
      logger.warn(msg, error);
    }

    @Override
    public void error(String msg) {
      logger.error(msg);
    }

    @Override
    public void error(String msg, Throwable error) {
      logger.error(msg, error);
    }
  }
}
