/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UrlUtilsTestCase
{
    @Test
    public void replaceRelativeBaseUriWithFull()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeUri, "http://localhost:8081/api");
        assertTrue(newRaml.contains("baseUri: http://localhost:8081/api"));
    }

    @Test
    public void replaceRelativeBaseUriWithRelative()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeUri, "/newapi");
        assertTrue(newRaml.contains("baseUri: /newapi"));
    }

    @Test
    public void replaceFullBaseUriWithFull()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlFullUri, "http://localhost:8081/api");
        assertTrue(newRaml.contains("baseUri: http://localhost:8081/api"));
    }

    @Test
    public void replaceFullBaseUriWithRelative()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlFullUri, "/newapi");
        assertTrue(newRaml.contains("baseUri: /newapi"));
    }

    @Test
    public void replaceFullBaseUriWithRelativeTwoDots()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlFullUri, "../api");
        assertTrue(newRaml.contains("baseUri: ../api"));
    }

    @Test
    public void replaceFullBaseUriWithRelativeWithoutSlash()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlFullUri, "api");
        assertTrue(newRaml.contains("baseUri: api"));
    }

    @Test
    public void replaceRelativeBaseUriWithRelativeTwoDots()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeUri, "../api");
        assertTrue(newRaml.contains("baseUri: ../api"));
    }

    @Test
    public void replaceRelativeBaseUriWithRelativeWithoutSlash()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeUri, "api");
        assertTrue(newRaml.contains("baseUri: api"));
    }

    @Test
    public void replaceRelativeTwoDotsBaseUriWithFull()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeTwoDotsUri, "http://hola.com/api");
        assertTrue(newRaml.contains("baseUri: http://hola.com/api"));
    }

    @Test
    public void replaceRelativeTwoDotsBaseUriWithRelative()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeTwoDotsUri, "/api");
        assertTrue(newRaml.contains("baseUri: /api"));
    }

    @Test
    public void replaceRelativeTwoDotsBaseUriWithRelativeTwoDots()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeTwoDotsUri, "../api2");
        assertTrue(newRaml.contains("baseUri: ../api2"));
    }

    @Test
    public void replaceRelativeTwoDotsBaseUriWithRelativeWithoutSlash()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeTwoDotsUri, "api2");
        assertTrue(newRaml.contains("baseUri: api2"));
    }

    @Test
    public void replaceRelativeWithoutSlashBaseUriWithRelativeWithoutSlash()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeWithoutSlashUri, "api2");
        assertTrue(newRaml.contains("baseUri: api2"));
    }

    @Test
    public void replaceRelativeWithoutSlashBaseUriWithFull()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeWithoutSlashUri, "http://hello.com/api2");
        assertTrue(newRaml.contains("baseUri: http://hello.com/api2"));
    }

    @Test
    public void replaceRelativeWithoutSlashBaseUriWithRelative()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeWithoutSlashUri, "/api2");
        assertTrue(newRaml.contains("baseUri: /api2"));
    }

    @Test
    public void replaceRelativeWithoutSlashBaseUriWithRelativeTwoDots()
    {
        String newRaml = UrlUtils.replaceBaseUri(ramlRelativeWithoutSlashUri, "../api2");
        assertTrue(newRaml.contains("baseUri: ../api2"));
    }

    String ramlRelativeUri = "#%RAML 1.0\n" +
                  "title: Admin\n" +
                  "version: 1.0\n" +
                  "baseUri: /api\n";

    String ramlFullUri = "#%RAML 1.0\n" +
                             "title: Admin\n" +
                             "version: 1.0\n" +
                             "baseUri: http://localhost:9090/api\n";

    String ramlRelativeTwoDotsUri = "#%RAML 1.0\n" +
                         "title: Admin\n" +
                         "version: 1.0\n" +
                         "baseUri: ../api\n";

    String ramlRelativeWithoutSlashUri = "#%RAML 1.0\n" +
                                    "title: Admin\n" +
                                    "version: 1.0\n" +
                                    "baseUri: api\n";

}
