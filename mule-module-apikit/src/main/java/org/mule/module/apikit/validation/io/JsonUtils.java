package org.mule.module.apikit.validation.io;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;

public class JsonUtils
{
    private static final String JSON_STRICT_DUPLICATE_DETECTION_PROPERTY = "yagi.json_duplicate_keys_detection";

    public static JsonNode parseJson(Reader reader) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, getSystemPropValue());
        return mapper.readValue(reader, JsonNode.class);
    }

    private static boolean getSystemPropValue()
    {
        return Boolean.valueOf(System.getProperty(JSON_STRICT_DUPLICATE_DETECTION_PROPERTY, "true"));
    }

}
