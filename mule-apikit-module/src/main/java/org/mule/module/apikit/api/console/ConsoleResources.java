/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.console;

import org.apache.commons.io.IOUtils;
import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.api.config.ConsoleConfig;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.raml.interfaces.model.ApiVendor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mule.module.apikit.api.Parser.AMF;
import static org.mule.module.apikit.api.UrlUtils.getCompletePathFromBasePathAndPath;

public class ConsoleResources {

  private static final String ROOT_CONSOLE_PATH = "/";
  private static final String INDEX_RESOURCE_RELATIVE_PATH = "/index.html";
  private static final String RAML_LOCATION_PLACEHOLDER_KEY = "RAML_LOCATION_PLACEHOLDER";
  private static final String HTTP_LISTENER_BASE_PATH = "HTTP_LISTENER_BASE_PATH";
  private static final String AMF_MODEL_LOCATION = "AMF_MODEL_LOCATION";

  private final String CONSOLE_RESOURCES_BASE;
  private ConsoleConfig config;
  private String listenerPath;
  private String requestPath;
  private String queryString;
  private String method;
  private String aceptHeader;

  public ConsoleResources(ConsoleConfig config, String listenerPath, String requestPath, String queryString, String method,
                          String aceptHeader) {
    CONSOLE_RESOURCES_BASE = AMF == config.getParser() ? "/console-resources-amf" : "/console-resources";

    this.config = config;
    this.listenerPath = listenerPath;
    this.requestPath = requestPath;
    this.queryString = queryString;
    this.method = method;
    this.aceptHeader = aceptHeader;
  }

  public Resource getConsoleResource(String resourceRelativePath) {

    // For getting RAML resources
    String raml = getRamlResourceIfRequested(resourceRelativePath);
    if (raml != null) {
      return new RamlResource(raml);
    }

    String consoleResourcePath;
    InputStream inputStream = null;
    ByteArrayOutputStream byteArrayOutputStream = null;

    try {
      if (resourceRelativePath.equals(ROOT_CONSOLE_PATH)) {
        consoleResourcePath = CONSOLE_RESOURCES_BASE + INDEX_RESOURCE_RELATIVE_PATH;
      } else {
        consoleResourcePath = CONSOLE_RESOURCES_BASE + resourceRelativePath;
      }

      inputStream = getClass().getResourceAsStream(consoleResourcePath);

      if (inputStream == null) {
        raml = config.getRamlHandler().getRamlV2(resourceRelativePath);
        if (raml == null) {
          throw ApikitErrorTypes.throwErrorType(new NotFoundException(resourceRelativePath));
        }

        return new RamlResource(raml);
      }

      if (consoleResourcePath.contains("index.html")) {
        inputStream = updateIndexWithRamlLocation(inputStream);
      }

      byteArrayOutputStream = new ByteArrayOutputStream();
      IOUtils.copyLarge(inputStream, byteArrayOutputStream);

      return new ConsoleResource(byteArrayOutputStream.toByteArray(), consoleResourcePath);

    } catch (IOException e) {
      throw ApikitErrorTypes.throwErrorType(new NotFoundException(resourceRelativePath));
    } finally {
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(byteArrayOutputStream);
    }


  }

  private InputStream updateIndexWithRamlLocation(InputStream inputStream) throws IOException {
    String ramlLocation;
    if (config.getRamlHandler().getApiVendor().equals(ApiVendor.RAML_10)) {
      ramlLocation = config.getRamlHandler().getRootRamlLocationForV2();
    } else {
      ramlLocation = config.getRamlHandler().getRootRamlLocationForV1();
    }


    String indexHtml = IOUtils.toString(inputStream);
    IOUtils.closeQuietly(inputStream);

    // for amf console index
    indexHtml = indexHtml.replaceFirst(HTTP_LISTENER_BASE_PATH, getCompletePathFromBasePathAndPath("", listenerPath));
    indexHtml = indexHtml.replaceFirst(AMF_MODEL_LOCATION, listenerPath + "/?amf");

    indexHtml = indexHtml.replaceFirst(RAML_LOCATION_PLACEHOLDER_KEY, ramlLocation);
    inputStream = new ByteArrayInputStream(indexHtml.getBytes());

    return inputStream;
  }

  /**
   * Validates if the path specified in the listener is a valid one. In order to this to be valid, path MUST end with "/*".
   * Example: path="/whatever/your/path/is/*"
   *
   * @param listenerPath Path specified in the listener element of the console
   */
  public void isValidPath(String listenerPath) {
    if (listenerPath != null && !listenerPath.endsWith("/*")) {
      throw new IllegalStateException("Console path in listener must end with /*");
    }
  }

  public String getRamlResourceIfRequested(String resourceRelativePath) {
    if (config.getRamlHandler().isRequestingRamlV1ForConsole(listenerPath, requestPath, queryString, method, aceptHeader)) {
      return config.getRamlHandler().getRamlV1();
    }

    if (config.getRamlHandler().isRequestingRamlV2(listenerPath, requestPath, queryString, method)) {
      return config.getRamlHandler().getRamlV2(resourceRelativePath);
    }

    if (AMF == config.getParser() && queryString.equals("amf")) {
      return config.getRamlHandler().getAMFModel();
    }

    return null;
  }
}
