/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import amf.client.environment.DefaultEnvironment;
import amf.client.environment.Environment;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.mule.amf.impl.ParserWrapperAmf;
import org.mule.raml.implv1.ParserV1Utils;
import org.mule.raml.implv1.ParserWrapperV1;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.implv2.ParserWrapperV2;
import org.mule.raml.implv2.loader.ExchangeDependencyResourceLoader;
import org.mule.raml.interfaces.ParserWrapper;

import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.parser.rule.IValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.mule.raml.interfaces.parser.rule.Severity;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.model.ScaffolderReport;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;
import org.mule.tools.apikit.model.ScaffolderResourceLoaderWrapper;
import org.mule.tools.apikit.output.GenerationModel;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.FileResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.loader.RootRamlFileResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.mule.raml.interfaces.parser.rule.Severity.WARNING;

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

  public RAMLFilesParser(Log log, Map<File, InputStream> fileStreams, APIFactory apiFactory) {
    this.log = log;
    this.apiFactory = apiFactory;
    List<File> processedFiles = new ArrayList<>();
    for (Map.Entry<File, InputStream> fileInputStreamEntry : fileStreams.entrySet()) {
      String content;
      File ramlFile = fileInputStreamEntry.getKey();
      try {
        content = IOUtils.toString(fileInputStreamEntry.getValue());
      } catch (IOException ioe) {
        this.log.info("Error loading file " + ramlFile.getName());
        break;

      }

      try {
        final ParserWrapper parserWrapper = getParserWrapper(ramlFile, content);
        parserWrapper.validate(); // This will fail whether the raml is not valid

        vendorId = parserWrapper.getApiVendor().toString();
        final IRaml raml = parserWrapper.build();
        ramlVersion = raml.getVersion();

        collectResources(ramlFile.getAbsolutePath(), raml.getResources(), API.DEFAULT_BASE_URI, raml.getVersion());
        processedFiles.add(ramlFile);
      } catch (Exception e) {
        final String reason = e.getMessage() == null ? "" : " Reason: " + e.getMessage();
        log.info("Could not parse [" + ramlFile + "] as root RAML file." + reason);
        log.debug(e);
      }

    }
    if (processedFiles.size() > 0) {
      this.log.info("The following RAML files were parsed correctly: " +
          processedFiles);
    } else {
      parseStatus = ScaffolderReport.FAILED;
      errorMessage = "RAML Root not found. None of the files were recognized as valid root RAML files.";
      this.log.error(errorMessage);
    }
  }

  private ResourceLoader getResourceLoader(String ramlFolderPath, File ramlFileParent) {
    return new CompositeResourceLoader(new RootRamlFileResourceLoader(ramlFileParent), new DefaultResourceLoader(),
                                       new FileResourceLoader(ramlFolderPath),
                                       new ExchangeDependencyResourceLoader(ramlFolderPath));
  }

  public RAMLFilesParser(Log log, Map<String, InputStream> ramls, ScaffolderResourceLoader scaffolderResourceLoader,
                         APIFactory apiFactory) {
    this.log = log;
    this.apiFactory = apiFactory;
    List<String> processedRamls = new ArrayList<>();
    for (Map.Entry<String, InputStream> fileInputStreamEntry : ramls.entrySet()) {
      String content;
      String rootRamlName = fileInputStreamEntry.getKey();
      try {
        content = IOUtils.toString(fileInputStreamEntry.getValue());
      } catch (IOException ioe) {
        this.log.info("Error loading file " + rootRamlName);
        break;

      }

      try {
        final ParserWrapper parserWrapper = getParserWrapper(null, content, rootRamlName, scaffolderResourceLoader);
        parserWrapper.validate(); // This will fail whether the raml is not valid

        vendorId = parserWrapper.getApiVendor().toString();
        final IRaml raml = parserWrapper.build();
        ramlVersion = raml.getVersion();

        collectResources(rootRamlName, raml.getResources(), API.DEFAULT_BASE_URI, raml.getVersion());
        processedRamls.add(rootRamlName);
      } catch (Exception e) {
        final String reason = e.getMessage() == null ? "" : " Reason: " + e.getMessage();
        log.info("Could not parse [" + rootRamlName + "] as root RAML." + reason);
        log.debug(e);
      }

    }
    if (processedRamls.size() > 0) {
      this.log.info("The following RAML files were parsed correctly: " +
          processedRamls);
    } else {
      parseStatus = ScaffolderReport.FAILED;
      errorMessage = "RAML Root not found. None of the files were recognized as valid root RAML files.";
      this.log.error(errorMessage);
    }
  }


  void collectResources(String filePath, Map<String, IResource> resourceMap, String baseUri, String version) {
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

  private ParserWrapper getParserWrapper(File apiFile, String content) throws Exception {
    return getParserWrapper(apiFile, content, null, null);
  }

  private ParserWrapper getParserWrapper(File apiFile, String content, String rootRamlPath,
                                         ScaffolderResourceLoader scaffolderResourceLoader)
      throws Exception {
    String apiFolderPath = null;
    File apiFileParent = null;
    if (apiFile != null && apiFile.getParentFile() != null) {
      apiFileParent = apiFile.getParentFile();
      apiFolderPath = apiFileParent.getPath();
    }

    ParserWrapper parserWrapper;

    // Used for Testing, we don't want fallback
    final String parserValue = System.getProperty(MULE_APIKIT_PARSER, "AUTO");
    if ("AMF".equals(parserValue)) {
      Environment environment = DefaultEnvironment.apply();
      if (scaffolderResourceLoader == null) {
        environment = environment.add(new org.mule.amf.impl.loader.ExchangeDependencyResourceLoader(apiFolderPath));
        parserWrapper = ParserWrapperAmf.create(apiFile.toURI(), environment, false);
      } else {
        environment = environment.add(new ScaffolderResourceLoaderWrapper(scaffolderResourceLoader, rootRamlPath));
        parserWrapper = ParserWrapperAmf.create(rootRamlPath, environment, false);
      }
      log.info(buildParserInfoMessage("AMF"));
      return parserWrapper;
    }

    if ("RAML".equals(parserValue)) {
      return applyFallback(apiFile, content, apiFolderPath, apiFileParent, Collections.emptyList(), rootRamlPath,
                           scaffolderResourceLoader);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    try {
      Environment environment = DefaultEnvironment.apply();
      if (scaffolderResourceLoader == null) {
        environment = environment.add(new org.mule.amf.impl.loader.ExchangeDependencyResourceLoader(apiFolderPath));
        parserWrapper = ParserWrapperAmf.create(apiFile.toURI(), environment, false);
      } else {
        environment = environment.add(new ScaffolderResourceLoaderWrapper(scaffolderResourceLoader, rootRamlPath));
        parserWrapper = ParserWrapperAmf.create(rootRamlPath, environment, false);

      }
    } catch (Exception e) {
      final List<IValidationResult> errors = singletonList(IValidationResult.fromException(e));
      return applyFallback(apiFile, content, apiFolderPath, apiFileParent, errors, rootRamlPath, scaffolderResourceLoader);
    }

    final IValidationReport validationReport = parserWrapper.validationReport();
    if (validationReport.conforms()) {
      log.info(buildParserInfoMessage("AMF"));
      return parserWrapper;
    } else {
      final List<IValidationResult> errorsFound = validationReport.getResults();
      return applyFallback(apiFile, content, apiFolderPath, apiFileParent, errorsFound, rootRamlPath, scaffolderResourceLoader);
    }
  }

  private ParserWrapper applyFallback(File apiFile, String content, String apiFolderPath, File apiFileParent,
                                      List<IValidationResult> errorsFound, String rootRamlPath,
                                      ScaffolderResourceLoader scaffolderResourceLoader) {
    ParserWrapper parserWrapper;

    if (ParserV2Utils.useParserV2(content)) {
      ResourceLoader resourceLoader = null;
      if (scaffolderResourceLoader == null) {
        resourceLoader =
            new CompositeResourceLoader(new RootRamlFileResourceLoader(apiFileParent), new DefaultResourceLoader(),
                                        new FileResourceLoader(apiFolderPath),
                                        new ExchangeDependencyResourceLoader(apiFolderPath));
        parserWrapper = new ParserWrapperV2(apiFile.getPath(), resourceLoader);
      } else {
        ScaffolderResourceLoaderWrapper scaffolderResourceLoaderWrapper =
            new ScaffolderResourceLoaderWrapper(scaffolderResourceLoader, rootRamlPath);
        resourceLoader = new CompositeResourceLoader(new DefaultResourceLoader(), scaffolderResourceLoaderWrapper);
        parserWrapper = new ParserWrapperV2(rootRamlPath, resourceLoader);

      }
    } else {
      if (rootRamlPath != null) {
        parserWrapper = new ParserWrapperV1(rootRamlPath);
      } else {
        parserWrapper = new ParserWrapperV1(apiFile.getAbsolutePath());
      }
    }

    if (parserWrapper.validationReport().conforms()) {
      log.info(buildParserInfoMessage("RAML Parser"));
      logErrors(errorsFound, WARNING);
      return parserWrapper;
    } else {
      logErrors(errorsFound);
      throw new RuntimeException(buildErrorMessage(errorsFound));
    }
  }


  private String buildParserInfoMessage(String parser) {
    return format("Using %s to load APIs", parser);
  }

  private void logErrors(List<IValidationResult> validationResults) {
    validationResults.stream().forEach(error -> logError(error, error.getSeverity()));
  }

  private void logErrors(List<IValidationResult> validationResults, Severity overridenSeverity) {
    validationResults.stream().forEach(error -> logError(error, overridenSeverity));
  }

  private void logError(IValidationResult error, Severity severity) {
    if (severity == Severity.INFO)
      log.info(error.getMessage());
    else if (severity == WARNING)
      log.warn(error.getMessage());
    else
      log.error(error.getMessage());
  }

  private static String buildErrorMessage(List<IValidationResult> validationResults) {
    final StringBuilder message = new StringBuilder("Invalid API descriptor -- errors found: ");
    message.append(validationResults.size()).append("\n\n");
    for (IValidationResult error : validationResults) {
      message.append(error.getMessage()).append("\n");
    }
    return message.toString();
  }

}
