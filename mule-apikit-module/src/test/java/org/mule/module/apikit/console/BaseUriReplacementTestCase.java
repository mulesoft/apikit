/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import static org.junit.Assert.assertEquals;

import org.mule.module.apikit.api.RamlHandler;

import org.junit.AfterClass;
import org.junit.Test;

public class BaseUriReplacementTestCase
{
    @Test
    public void baseUriReplacementTest() throws Exception
    {
        RamlHandler ramlHandler = new RamlHandler("org/mule/module/apikit/console/simple-with-baseuri10.raml",false);
        assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
        assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

        System.setProperty("fullDomain", "pepe.cloudhub.io");
        assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
        assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

        System.setProperty("fullDomain", "http://pepe.cloudhub.io");
        assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
        assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

        System.setProperty("fullDomain", "http://pepe.cloudhub.io/");
        assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
        assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

        System.setProperty("fullDomain", "pepe.cloudhub.io/");
        assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
        assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

        System.setProperty("fullDomain", "pepe.cloudhub.io");
        assertEquals("http://localhost:8081/api/", ramlHandler.getBaseUriReplacement("http://localhost:8081/api/"));
        assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

        System.setProperty("fullDomain", "http://pepe.cloudhub.io");
        assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

        System.setProperty("fullDomain", "http://pepe.cloudhub.io/");
        assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

        System.setProperty("fullDomain", "pepe.cloudhub.io/");
        assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

        System.setProperty("fullDomain", "pepe.cloudhub.io");
        assertEquals("http://localhost:8081/", ramlHandler.getBaseUriReplacement("http://localhost:8081/"));
        assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

        System.setProperty("fullDomain", "http://pepe.cloudhub.io");
        assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

        System.setProperty("fullDomain", "http://pepe.cloudhub.io/");
        assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

        System.setProperty("fullDomain", "pepe.cloudhub.io/");
        assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

        System.setProperty("fullDomain", "pepe.cloudhub.io");
        assertEquals("http://localhost:8081", ramlHandler.getBaseUriReplacement("http://localhost:8081"));
        assertEquals("http://pepe.cloudhub.io", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

        System.setProperty("fullDomain", "http://pepe.cloudhub.io");
        assertEquals("http://pepe.cloudhub.io", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

        System.setProperty("fullDomain", "http://pepe.cloudhub.io/");
        assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

        System.setProperty("fullDomain", "pepe.cloudhub.io/");
        assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

        System.setProperty("fullDomain", "pepe.cloudhub.io/api");
        assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

        System.setProperty("fullDomain", "http://pepe.cloudhub.io/api");
        assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

        System.setProperty("fullDomain", "http://pepe.cloudhub.io/api");
        assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

        System.setProperty("fullDomain", "pepe.cloudhub.io/api");
        assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));
    }

    @AfterClass
    public static void after()
    {
        System.clearProperty("fullDomain");
    }
}
