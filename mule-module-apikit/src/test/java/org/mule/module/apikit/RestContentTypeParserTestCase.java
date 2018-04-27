/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class RestContentTypeParserTestCase
{

    private List<MediaType> parseType(String acceptedHeader)
    {
        return RestContentTypeParser.parseMediaTypes(acceptedHeader);
    }

    @Test
    public void validMediaTypesWithSpaces() throws Exception
    {
        String acceptedHeader = " application/json, application/xml";
        List<MediaType> expectedMediaTypes = new ArrayList<>();
        expectedMediaTypes.add(MediaType.create("application", "json"));
        expectedMediaTypes.add(MediaType.create("application", "xml"));
        List<MediaType> actualMediaTypes = RestContentTypeParser.parseMediaTypes(acceptedHeader);
        assertEquals(expectedMediaTypes, actualMediaTypes);
    }

    @Test
    public void asterisk() throws Exception
    {
        List<MediaType> actualMediaTypes = RestContentTypeParser.parseMediaTypes("*");
        assertTrue(actualMediaTypes.size() == 1);

    }

    @Test
    public void asteriskWithPrecedingSpace() throws Exception
    {
        assertAcceptHeader(" *");
    }

    @Test
    public void asteriskWithTrailingSpace() throws Exception
    {
        assertAcceptHeader("* ");
    }

    @Test
    public void slash() throws Exception
    {
        assertAcceptHeader("/");
    }

    @Test
    public void asteriskWithQuality() throws Exception
    {
        assertAcceptHeader("*; q=1");
    }

    @Test
    public void asteriskWithQualityAndPrecedingSpace() throws Exception
    {
        assertAcceptHeader(" *; q=1");
    }

    @Test
    public void alphanumeric() throws Exception
    {
        assertAcceptHeader("asd123*@");
    }

    private void assertAcceptHeader(String acceptedHeader)
    {
        List<MediaType> actualMediaTypes = RestContentTypeParser.parseMediaTypes(acceptedHeader);
        assertTrue(actualMediaTypes.size() == 0);
    }

    @Test
    public void complexMediaTypes() throws Exception
    {
        String acceptedHeader = "text/html, image/gif, image/jpeg, ​*; q=.2, */*; q=.2";
        List<MediaType> expectedMediaTypes = new ArrayList<>();
        expectedMediaTypes.add(MediaType.parse("text/html"));
        expectedMediaTypes.add(MediaType.parse("image/gif"));
        expectedMediaTypes.add(MediaType.parse("image/jpeg"));
        expectedMediaTypes.add(MediaType.parse("*/*; q=.2"));
        List<MediaType> actualMediaTypes = RestContentTypeParser.parseMediaTypes(acceptedHeader);
        assertEquals(expectedMediaTypes, actualMediaTypes);
    }

}
