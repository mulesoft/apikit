/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import amf.client.environment.DefaultEnvironment;
import amf.client.environment.Environment;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.mule.amf.impl.ParserWrapperAmf;
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

import static org.mule.raml.interfaces.parser.rule.Severity.WARNING;

public class RAMLFilesParser {

  private Map<ResourceActionMimeTypeTriplet, GenerationModel> entries =
      new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
  private final APIFactory apiFactory;
  private final Log log;

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

        final IRaml raml = parserWrapper.build();
        collectResources(ramlFile, raml.getResources(), API.DEFAULT_BASE_URI, raml.getVersion());
        processedFiles.add(ramlFile);
      } catch (Exception e) {
        log.info("Could not parse [" + ramlFile + "] as root RAML file. Reason: " + e.getMessage());
        log.debug(e);
      }

    }
    if (processedFiles.size() > 0) {
      this.log.info("The following RAML files were parsed correctly: " +
          processedFiles);
    } else {
      this.log.error("RAML Root not found. None of the files were recognized as valid root RAML files.");
    }
  }

  void collectResources(File filename, Map<String, IResource> resourceMap, String baseUri, String version) {
    for (IResource resource : resourceMap.values()) {
      for (IAction action : resource.getActions().values()) {


        API api = apiFactory.createAPIBinding(filename, null, baseUri, APIKitTools.getPathFromUri(baseUri, false), null, null);

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

      collectResources(filename, resource.getResources(), baseUri, version);
    }
  }

  void addResource(API api, IResource resource, IAction action, String mimeType, String version) {

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

  private ParserWrapper getParserWrapper(File apiFile, String content) {
    String apiFolderPath = null;
    File apiFileParent = null;
    if (apiFile.getParentFile() != null) {
      apiFileParent = apiFile.getParentFile();
      apiFolderPath = apiFileParent.getPath();
    }

    ParserWrapper parserWrapper;

    final Environment environment =
        DefaultEnvironment.apply().add(new org.mule.amf.impl.loader.ExchangeDependencyResourceLoader(apiFolderPath));
    parserWrapper = ParserWrapperAmf.create(apiFile.toURI(), environment, false);
    final IValidationReport validationReport = parserWrapper.validationReport();

    if (!validationReport.conforms()) {
      if (ParserV2Utils.useParserV2(content)) {
        final ResourceLoader resourceLoader =
            new CompositeResourceLoader(new RootRamlFileResourceLoader(apiFileParent), new DefaultResourceLoader(),
                                        new FileResourceLoader(apiFolderPath),
                                        new ExchangeDependencyResourceLoader(apiFolderPath));
        parserWrapper = new ParserWrapperV2(apiFile.getPath(), resourceLoader);
      } else {
        parserWrapper = new ParserWrapperV1(apiFile.getAbsolutePath());
      }

      final List<IValidationResult> foundErrors = validationReport.getResults();
      if (parserWrapper.validationReport().conforms()) {
        logErrors(foundErrors, WARNING);
      } else {
        raiseError(foundErrors);
      }
    }

    return parserWrapper;
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

  private void raiseError(List<IValidationResult> validationResults) {
    logErrors(validationResults);
    StringBuilder message = new StringBuilder("Invalid API descriptor -- errors found: ");
    message.append(validationResults.size()).append("\n\n");
    for (IValidationResult error : validationResults) {
      message.append(error.getMessage()).append("\n");
    }
    throw new RuntimeException(message.toString());
  }

}
