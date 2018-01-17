/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.exception.NotFoundException;

import java.io.IOException;

import org.junit.Test;
import org.mule.runtime.core.api.MuleContext;

public class RamlHandlerTestCase {

  private static MuleContext muleContext;

  @BeforeClass
  public static void beforeAll() {
    muleContext = mock(MuleContext.class);
    when(muleContext.getExecutionClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
  }

  @Test
  public void isParserV2FalseUsingRaml08() throws IOException {
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple08.raml";
    String apiServer = "unused";
    boolean keepRamlBaseUri = true;
    RamlHandler handler = new RamlHandler(ramlLocation, keepRamlBaseUri, muleContext);
    handler.setApiServer(apiServer);
    assertTrue(!handler.isParserV2());
  }

  @Test
  public void isParserV2TrueUsingRaml10() throws IOException {
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple10.raml";
    String apiServer = "unused";
    boolean keepRamlBaseUri = true;
    RamlHandler handler = new RamlHandler(ramlLocation, keepRamlBaseUri, muleContext);
    handler.setApiServer(apiServer);
    assertTrue(handler.isParserV2());
  }

  @Test
  public void getRamlV2KeepRamlBaseUriTrue() throws NotFoundException, IOException, IllegalAccessException {
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple10-with-example.raml";
    boolean keepRamlBaseUri = true;
    String apiServer = "http://www.newBaseUri.com";
    RamlHandler handler = new RamlHandler(ramlLocation, keepRamlBaseUri, muleContext);
    handler.setApiServer(apiServer);
    String rootRaml = handler.getRamlV2("org/mule/module/apikit/raml-handler/?raml");
    assertTrue(rootRaml.contains("RAML 1.0"));
    assertTrue(!rootRaml.contains(apiServer));
    assertTrue(rootRaml.contains("baseUri: http://localhost/myapi"));
  }

  @Test
  public void getRamlV2KeepRamlBaseUriFalse() throws NotFoundException, IOException, IllegalAccessException {
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple10-with-example.raml";//this.getClass().getResource("../../../../org/mule/module/apikit/simple-raml/simple10-with-example.raml").toString();
    boolean keepRamlBaseUri = false;
    String apiServer = "http://pepe.com";
    RamlHandler handler = new RamlHandler(ramlLocation, keepRamlBaseUri, muleContext);
    handler.setApiServer(apiServer);
    String rootRaml = handler.getRamlV2("org/mule/module/apikit/raml-handler/?raml");
    assertTrue(rootRaml.contains("baseUri: " + apiServer));
  }

  @Test
  public void getRamlV2Example() throws NotFoundException, IOException, IllegalAccessException {
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple10-with-example.raml";
    String apiServer = "unused";
    boolean keepRamlBaseUri = true;
    RamlHandler handler = new RamlHandler(ramlLocation, keepRamlBaseUri, muleContext);
    handler.setApiServer(apiServer);
    assertTrue(handler.getRamlV2("org/mule/module/apikit/raml-handler/example.json/?raml").contains("{\"name\":\"jane\"}"));
  }
}
