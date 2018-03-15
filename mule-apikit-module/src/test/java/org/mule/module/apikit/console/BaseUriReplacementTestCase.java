/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.api.UrlUtils;
import org.mule.runtime.core.api.MuleContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseUriReplacementTestCase {

  private static final String FULL_DOMAIN = UrlUtils.FULL_DOMAIN;

  private static MuleContext muleContext;

  @BeforeClass
  public static void beforeAll() {
    muleContext = mock(MuleContext.class);
    when(muleContext.getExecutionClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
  }

  @Test
  public void baseUriReplacementTest() throws Exception {
    RamlHandler ramlHandler = new RamlHandler("org/mule/module/apikit/console/simple-with-baseuri10.raml", false, muleContext);
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://localhost:8081/api", ramlHandler.getBaseUriReplacement("http://localhost:8081/api"));
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081/api/", ramlHandler.getBaseUriReplacement("http://localhost:8081/api/"));
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/api/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/api/"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081/", ramlHandler.getBaseUriReplacement("http://localhost:8081/"));
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081/"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io");
    assertEquals("http://localhost:8081", ramlHandler.getBaseUriReplacement("http://localhost:8081"));
    assertEquals("http://pepe.cloudhub.io", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io");
    assertEquals("http://pepe.cloudhub.io", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/");
    assertEquals("http://pepe.cloudhub.io/", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "http://pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));

    System.setProperty(FULL_DOMAIN, "pepe.cloudhub.io/api");
    assertEquals("http://pepe.cloudhub.io/api", ramlHandler.getBaseUriReplacement("http://0.0.0.0:8081"));
  }

  @Test
  public void consoleUriReplacementTest() throws Exception {
    // Console Replacements
    System.clearProperty(FULL_DOMAIN);
    assertEquals("http://localhost:8081/console", UrlUtils.getBaseUriReplacement("http://localhost:8081/console"));
    assertEquals("http://localhost:8081/console/", UrlUtils.getBaseUriReplacement("http://localhost:8081/console/"));

    System.setProperty(FULL_DOMAIN, "http://aamura.cloudhub.io/api");
    assertEquals("http://aamura.cloudhub.io/api/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("http://aamura.cloudhub.io/api/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    System.setProperty(FULL_DOMAIN, "http://aamura.cloudhub.io/api/");
    assertEquals("http://aamura.cloudhub.io/api/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("http://aamura.cloudhub.io/api/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    System.setProperty(FULL_DOMAIN, "https://aamura.cloudhub.io/api");
    assertEquals("https://aamura.cloudhub.io/api/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    System.setProperty(FULL_DOMAIN, "https://aamura.cloudhub.io/api/");
    assertEquals("https://aamura.cloudhub.io/api/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    System.setProperty(FULL_DOMAIN, "https://aamura.cloudhub.io/api/v1");
    assertEquals("https://aamura.cloudhub.io/api/v1/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/v1/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));

    System.setProperty(FULL_DOMAIN, "aamura.cloudhub.io/api/v1");
    assertEquals("https://aamura.cloudhub.io/api/v1/console", UrlUtils.getBaseUriReplacement("https://0.0.0.0:8081/console"));
    assertEquals("https://aamura.cloudhub.io/api/v1/console/", UrlUtils.getBaseUriReplacement("https://0.0.0.0:8081/console/"));

    assertEquals("http://aamura.cloudhub.io/api/v1/console", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console"));
    assertEquals("http://aamura.cloudhub.io/api/v1/console/", UrlUtils.getBaseUriReplacement("http://0.0.0.0:8081/console/"));
  }

  @AfterClass
  public static void after() {
    System.clearProperty(FULL_DOMAIN);
  }
}
