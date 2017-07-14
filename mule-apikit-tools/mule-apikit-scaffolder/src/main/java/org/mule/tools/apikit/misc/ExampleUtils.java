/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.weave.v2.module.reader.StringSourceProvider;
import org.mule.weave.v2.runtime.utils.WeaveSimpleRunner;
import org.yaml.snakeyaml.Yaml;
import scala.Option;

import java.io.IOException;

public class ExampleUtils
{
    private static final String DW_INPUT_TYPE = "payload";
    private static final String DW_OUTPUT_TYPE = "application/dw";
    private static final String APPLICATION_XML_CONTENT_TYPE = "application/xml";
    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    private ExampleUtils() { }

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

    public static String getDataWeaveExpressionText(String example)
    {
        String transformContentType = getExampleContentType(example);
        example = getExampleAsJSONIfNeeded(example);

        WeaveSimpleRunner runner = new WeaveSimpleRunner();
        runner.addInput(DW_INPUT_TYPE, transformContentType, new StringSourceProvider(example, Option.empty()));
        runner.setOutputType(DW_OUTPUT_TYPE);
        String weaveResult = runner.execute(DW_INPUT_TYPE).toString();

        return "%dw 2.0\n" +
                "output "+ transformContentType +"\n" +
                "---\n" + weaveResult + "\n";
    }

    private static String transformYamlExampleIntoJSON(String example)
    {
        Yaml yaml = new Yaml();
        Object yamlObject = yaml.load(example);

        try
        {
            return new ObjectMapper().writeValueAsString(yamlObject);
            
        } catch (JsonProcessingException e)
        {
            // If example couldn't have been processed, we return a null JSON.
            return "null";
        }
    }

    public static boolean isValidXML(String payload) {
        return payload.startsWith("<");
    }

    public static boolean isValidJSON(String payload) {

        try
        {
            new ObjectMapper().readTree(payload);

        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
