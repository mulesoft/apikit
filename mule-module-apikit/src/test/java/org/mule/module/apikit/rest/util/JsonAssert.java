/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.util;

import org.mule.util.IOUtils;

import java.io.IOException;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 *
 */
public class JsonAssert
{

    public static void compareJsonAsString(String expectedJsonFile, String actualJsonAsString) throws IOException, JSONException
    {
        String expectedJson = IOUtils.getResourceAsString(expectedJsonFile, JsonAssert.class);
        JSONAssert.assertEquals(expectedJson, actualJsonAsString,false);
    }
}
