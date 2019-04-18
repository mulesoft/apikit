/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.mule.weave.v2.runtime.DataWeaveResult;
import org.mule.weave.v2.runtime.DataWeaveScriptingEngine;
import org.mule.weave.v2.runtime.ScriptingBindings;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExampleUtils {

  private static final String APPLICATION_XML_CONTENT_TYPE = "application/xml";
  private static final String DEFAULT_CONTENT_TYPE = "application/json";

  private ExampleUtils() {}

  public static String getExampleContentType(String example) {

    if (isValidXML(example)) {
      return APPLICATION_XML_CONTENT_TYPE;
    }

    return DEFAULT_CONTENT_TYPE;
  }

  public static String getExampleAsJSONIfNeeded(String payload) {

    if (!(isValidXML(payload) || isValidJSON(payload))) {
      return transformYamlExampleIntoJSON(payload);
    }

    return payload;
  }

  public static String getDataWeaveExpressionText(String example) {
    String transformContentType = getExampleContentType(example);
    example = getExampleAsJSONIfNeeded(example);

    final String weaveResult = asDataWeave(example, transformContentType);

    return "%dw 2.0\n" +
        "output " + transformContentType + "\n" +
        "---\n" + weaveResult + "\n";
  }

  private static String asDataWeave(String payload, String mimeType) {
    Charset utf8 = Charset.forName("UTF-8");
    boolean isCorrectEncoding = Charset.defaultCharset().equals(utf8);
    if (!isCorrectEncoding) {
      byte[] payloadAsByteArray = payload.getBytes();
      payload = new String(payloadAsByteArray, utf8);
    }

    String script = "output application/dw --- payload";
    ScriptingBindings bindings = new ScriptingBindings()
        .addBinding("payload", payload, mimeType);
    DataWeaveResult result = DataWeaveScriptingEngine.write(script, bindings);

    if (!isCorrectEncoding) {
      try {
        InputStream content = (InputStream) result.getContent();
        String contentAsString = IOUtils.toString(content, utf8);
        IOUtils.closeQuietly(content);
        return contentAsString;
      } catch (IOException e) {
        return result.getContentAsString();
      }
    }

    return result.getContentAsString();
  }

  private static String transformYamlExampleIntoJSON(String example) {
    Yaml yaml = new Yaml();
    Object yamlObject = yaml.load(example);

    try {
      return new ObjectMapper().disableDefaultTyping().writeValueAsString(yamlObject);

    } catch (JsonProcessingException e) {
      return example;
    }
  }

  public static boolean isValidXML(String payload) {
    return payload.startsWith("<");
  }

  public static boolean isValidJSON(String payload) {

    try {
      new ObjectMapper().disableDefaultTyping().readTree(payload);

    } catch (IOException e) {
      return false;
    }

    return true;
  }
}
