/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.io;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.Reader;

public class JsonUtils
{
    private static final String JSON_STRICT_DUPLICATE_DETECTION_PROPERTY = "yagi.json_duplicate_keys_detection";
    private static final String JSON_BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES_PROPERTY = "yagi.json_block_unsafe_polymorphic_base_types";

    public static JsonNode parseJson(Reader reader) throws IOException {
        ObjectMapper mapper = JsonMapper.builder()
                .deactivateDefaultTyping()
                .configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, getSystemPropValue(JSON_STRICT_DUPLICATE_DETECTION_PROPERTY))
                .configure(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES,
                        getSystemPropValue(JSON_BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES_PROPERTY))
                .build();
        return mapper.readValue(reader, JsonNode.class);
    }

    private static boolean getSystemPropValue(String property) {
        return Boolean.valueOf(System.getProperty(property, "true"));
    }

}
