/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v1.io;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;

public class JsonUtils {

  private static final String JSON_STRICT_DUPLICATE_DETECTION_PROPERTY = "yagi.json_duplicate_keys_detection";

  public static JsonNode parseJson(Reader reader) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.disableDefaultTyping();
    mapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, getSystemPropValue());
    return mapper.readValue(reader, JsonNode.class);
  }

  private static boolean getSystemPropValue() {
    return Boolean.valueOf(System.getProperty(JSON_STRICT_DUPLICATE_DETECTION_PROPERTY, "true"));
  }

}
