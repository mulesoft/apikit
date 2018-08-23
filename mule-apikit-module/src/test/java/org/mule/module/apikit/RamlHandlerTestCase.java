/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.module.apikit.api.Parser;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.runtime.core.api.MuleContext;

import java.io.IOException;
import java.util.function.Supplier;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.module.apikit.api.Parser.AMF;
import static org.mule.module.apikit.api.Parser.AUTO;
import static org.mule.module.apikit.api.Parser.RAML;

public class RamlHandlerTestCase {

  private static MuleContext muleContext;

  @BeforeClass
  public static void beforeAll() {
    muleContext = mock(MuleContext.class);
    when(muleContext.getExecutionClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
  }

  @Test
  public void apiVendorForRaml08() throws IOException {
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple08.raml";
    String apiServer = "unused";
    RamlHandler handler = createRamlHandler(ramlLocation, true);
    handler.setApiServer(apiServer);
    assertTrue(handler.getApiVendor().equals(ApiVendor.RAML_08));
  }

  @Test
  public void isParserV2TrueUsingRaml10() throws IOException {
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple10.raml";
    String apiServer = "unused";
    RamlHandler handler = createRamlHandler(ramlLocation);
    handler.setApiServer(apiServer);
    assertTrue(handler.isParserV2());
    assertTrue(handler.getApiVendor().equals(ApiVendor.RAML_10));
  }

  @Test
  public void getRamlV2KeepRamlBaseUriTrue() throws IOException {
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple10-with-example.raml";
    boolean keepRamlBaseUri = true;
    String apiServer = "http://www.newBaseUri.com";
    RamlHandler handler = createRamlHandler(ramlLocation, keepRamlBaseUri);
    handler.setApiServer(apiServer);
    String rootRaml = handler.getRamlV2("org/mule/module/apikit/raml-handler/?raml");
    assertTrue(rootRaml.contains("RAML 1.0"));
    assertTrue(!rootRaml.contains(apiServer));
    assertTrue(rootRaml.contains("baseUri: http://localhost/myapi"));
  }

  @Test
  public void getRamlV2KeepRamlBaseUriFalse() throws IOException {
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple10-with-example.raml";//this.getClass().getResource("../../../../org/mule/module/apikit/simple-raml/simple10-with-example.raml").toString();
    String apiServer = "http://pepe.com";
    RamlHandler handler = createRamlHandler(ramlLocation, false);
    handler.setApiServer(apiServer);
    String rootRaml = handler.getRamlV2("org/mule/module/apikit/raml-handler/?raml");
    assertTrue(rootRaml.contains("baseUri: " + apiServer));
  }

  @Test
  public void getRamlV2Example() throws IOException {
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple10-with-example.raml";
    String apiServer = "unused";
    RamlHandler handler = createRamlHandler(ramlLocation);
    handler.setApiServer(apiServer);
    assertTrue(handler.getRamlV2("org/mule/module/apikit/raml-handler/example.json/?raml").contains("{\"name\":\"jane\"}"));
  }

  @Test
  public void testInitializationUsingAUTO() throws IOException {
    RamlHandler handler;

    final boolean keepRamlBaseUri = true;

    handler = createRamlHandler("org/mule/module/apikit/raml-handler/amf-only.raml", keepRamlBaseUri, AUTO);
    assertEquals(AMF, handler.getParser());

    handler = createRamlHandler("org/mule/module/apikit/raml-handler/raml-parser-only.raml", keepRamlBaseUri, AUTO);
    assertEquals(RAML, handler.getParser());

    assertException(RuntimeException.class, "Invalid API descriptor -- errors found: 2",
                    () -> createRamlHandler("org/mule/module/apikit/raml-handler/failing-api.raml", keepRamlBaseUri, AUTO));
  }

  @Test
  public void testInitializationUsingAMF() throws IOException {
    RamlHandler handler;

    final boolean keepRamlBaseUri = true;

    handler = createRamlHandler("org/mule/module/apikit/raml-handler/amf-only.raml", keepRamlBaseUri, AMF);
    assertEquals(AMF, handler.getParser());

    assertException(RuntimeException.class, "Invalid API descriptor -- errors found: 1",
                    () -> createRamlHandler("org/mule/module/apikit/raml-handler/raml-parser-only.raml", keepRamlBaseUri, AMF));
    assertException(RuntimeException.class, "Invalid API descriptor -- errors found: 2",
                    () -> createRamlHandler("org/mule/module/apikit/raml-handler/failing-api.raml", keepRamlBaseUri, AMF));
  }

  @Test
  public void testInitializationUsingRAML() throws IOException {
    RamlHandler handler;

    final boolean keepRamlBaseUri = true;

    handler = createRamlHandler("org/mule/module/apikit/raml-handler/raml-parser-only.raml", keepRamlBaseUri, RAML);
    assertEquals(RAML, handler.getParser());

    assertException(RuntimeException.class, "Invalid API descriptor -- errors found: 1",
                    () -> createRamlHandler("org/mule/module/apikit/raml-handler/amf-only.raml", keepRamlBaseUri, RAML));
    assertException(RuntimeException.class, "Invalid API descriptor -- errors found: 1\n\nInvalid reference 'SomeTypo'",
                    () -> createRamlHandler("org/mule/module/apikit/raml-handler/failing-api.raml", keepRamlBaseUri, RAML));
  }

  private <A, B> void assertException(Class<A> clazz, String message, Supplier<B> supplier) {
    try {
      supplier.get();
      fail(clazz.getName() + " was expected");
    } catch (Exception e) {
      assertEquals(clazz, e.getClass());
      assertTrue(e.getMessage().contains(message));
    }
  }

  private RamlHandler createRamlHandler(String ramlPath) {
    return createRamlHandler(ramlPath, true, AUTO);
  }

  private RamlHandler createRamlHandler(String ramlPath, boolean keepRamlBaseUri) {
    return createRamlHandler(ramlPath, keepRamlBaseUri, AUTO);
  }

  private RamlHandler createRamlHandler(String ramlPath, boolean keepRamlBaseUri, Parser parser) {
    try {
      return new RamlHandler(ramlPath, keepRamlBaseUri, muleContext, parser);
    } catch (IOException e) {
      throw new RuntimeException("Error creating RamlHandler", e);
    }
  }
}
