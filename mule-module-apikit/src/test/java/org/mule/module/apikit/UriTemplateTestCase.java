/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.mule.module.apikit.uri.URICoder;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolveResult;
import org.mule.module.apikit.uri.URIResolver;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Ignore;
import org.junit.Test;

public class UriTemplateTestCase
{

    private static final String HOST = "hi.com";
    private static final String URL_PREFIX = "http://" + HOST;

    private static char[] SPECIAL_CHARS_URI_ENCODED = {'"','?','[',']','{','}','\\'};
    private static char[] SPECIAL_CHARS_NOT_URI_ENCODED = {',',';',':','\'','=','!','*','@','(',')','/'};

    @Test
    public void alphaCharacters()
    {
        matchSingleTemplateLevelOne("onetwo");
        matchSingleTemplateLevelOne("TWOONE");
    }

    @Test
    public void spaceCharacter()
    {
        matchSingleTemplateLevelOne("one two");
        matchSingleTemplateLevelOne(" one two  three ");
    }

    @Test
    public void specialUriEncodedCharactersLevelOne()
    {
        for (char c : SPECIAL_CHARS_URI_ENCODED)
        {
            matchSingleTemplateLevelOne("one" + c + "two");
        }
    }

    @Test
    public void specialNotUriEncodedCharactersLevelOne()
    {
        for (char c : SPECIAL_CHARS_NOT_URI_ENCODED)
        {
            matchMultiTemplateLevelOne("one" + c + "two");
        }
    }

    @Test
    @Ignore //level two templates not supported as per raml spec
    public void specialNotUriEncodedCharactersLevelTwo()
    {
        for (char c : SPECIAL_CHARS_NOT_URI_ENCODED)
        {
            matchSingleTemplateLevelTwo("one" + c + "two");
        }
    }

    @Test
    public void plusCharacter()
    {
        matchMultiTemplateLevelOne("one+two");

        //TODO probably a bug in the ASCII decoder that converts '+' into ' ' when it should not.
        //matchSingleTemplateLevelTwo("one+two");
    }

    @Test
    public void percentEncodeReservedCharacters()
    {
        for (char c : SPECIAL_CHARS_URI_ENCODED)
        {
            matchSingleTemplateLevelOne(URICoder.encode("one" + c + "two"));
        }
        for (char c : SPECIAL_CHARS_NOT_URI_ENCODED)
        {
            matchSingleTemplateLevelOne(URICoder.encode("one" + c + "two"));
        }
    }

    private void matchSingleTemplateLevelOne(String value)
    {
        URIResolver resolver = new URIResolver(getUri(value));
        URIPattern pattern = new URIPattern(URL_PREFIX + "/{id}");
        URIResolveResult resolve = resolver.resolve(pattern);
        assertThat((String) resolve.get("id"), is(URICoder.encode(value)));
    }

    private void matchSingleTemplateLevelTwo(String value)
    {
        URIResolver resolver = new URIResolver(getUri(value));
        URIPattern pattern = new URIPattern(URL_PREFIX + "/{+id}");
        URIResolveResult resolve = resolver.resolve(pattern);
        assertThat((String) resolve.get("id"), is(value));
    }

    private void matchMultiTemplateLevelOne(String value)
    {
        URIResolver resolver = new URIResolver(getUri(value));
        char sep = value.charAt(3);
        URIPattern pattern = new URIPattern(URL_PREFIX + "/{id1}" + sep + "{id2}");
        URIResolveResult resolve = resolver.resolve(pattern);
        assertThat((String) resolve.get("id1"), is("one"));
        assertThat((String) resolve.get("id2"), is("two"));
    }

    private String getUri(String value)
    {
        URI uri;
        try
        {
            uri = new URI("http", HOST, "/" + value, null);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        return uri.toString();
    }

}
