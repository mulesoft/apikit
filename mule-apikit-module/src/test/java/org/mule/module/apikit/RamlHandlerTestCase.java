/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.parser.service.ParserServiceException;
import org.mule.raml.interfaces.ParserType;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.runtime.core.api.MuleContext;

import java.io.IOException;
import java.util.function.Supplier;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.raml.interfaces.ParserType.AMF;
import static org.mule.raml.interfaces.ParserType.AUTO;
import static org.mule.raml.interfaces.ParserType.RAML;

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
  public void addLocalHostAsServerWhenIsNotDefined() throws IOException {
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple10.raml";
    boolean keepRamlBaseUri = false;
    RamlHandler handler = createRamlHandler(ramlLocation, keepRamlBaseUri);
    handler.setApiServer("localhost:8081/");
    String rootRaml = handler.getAMFModel();
    assertTrue(rootRaml.contains("localhost:8081/"));
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
    String ramlLocation = "org/mule/module/apikit/raml-handler/simple10-with-example.raml";// this.getClass().getResource("../../../../org/mule/module/apikit/simple-raml/simple10-with-example.raml").toString();
    String apiServer = "http://pepe.com";
    RamlHandler handler = createRamlHandler(ramlLocation, false);
    handler.setApiServer(apiServer);

    String ramlV1 = handler.getRamlV1();
    assertTrue(ramlV1.contains("baseUri: " + apiServer));

    String ramlV2 = handler.getRamlV2("org/mule/module/apikit/raml-handler/?raml");
    assertTrue(ramlV2.contains("baseUri: " + apiServer));

    String ramlAmf = handler.getAMFModel();
    assertTrue(ramlAmf.contains("\"" + apiServer + "\""));
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
    assertEquals(AMF, handler.getParserType());

    handler = createRamlHandler("org/mule/module/apikit/raml-handler/raml-parser-only.raml", keepRamlBaseUri, AUTO);
    assertEquals(RAML, handler.getParserType());

    assertException(RuntimeException.class, "Invalid API descriptor -- errors found: 1",
                    () -> createRamlHandler("org/mule/module/apikit/raml-handler/failing-api.raml", keepRamlBaseUri, AUTO));
  }

  @Test
  public void testInitializationUsingAMF() throws IOException {
    RamlHandler handler;

    final boolean keepRamlBaseUri = true;

    handler = createRamlHandler("org/mule/module/apikit/raml-handler/amf-only.raml", keepRamlBaseUri, AMF);
    assertEquals(AMF, handler.getParserType());

    assertException(ParserServiceException.class, "Invalid API descriptor -- errors found: 1",
                    () -> createRamlHandler("org/mule/module/apikit/raml-handler/raml-parser-only.raml", keepRamlBaseUri, AMF));
    assertException(ParserServiceException.class, "Invalid API descriptor -- errors found: 1",
                    () -> createRamlHandler("org/mule/module/apikit/raml-handler/failing-api.raml", keepRamlBaseUri, AMF));
  }

  @Test
  public void testInitializationUsingRAML() throws IOException {
    RamlHandler handler;

    final boolean keepRamlBaseUri = true;

    handler = createRamlHandler("org/mule/module/apikit/raml-handler/raml-parser-only.raml", keepRamlBaseUri, RAML);
    assertEquals(RAML, handler.getParserType());

    assertException(RuntimeException.class, "Invalid API descriptor -- errors found: 1",
                    () -> createRamlHandler("org/mule/module/apikit/raml-handler/amf-only.raml", keepRamlBaseUri, RAML));
    assertException(RuntimeException.class, "Invalid API descriptor -- errors found: 1\n\nInvalid reference 'SomeTypo'",
                    () -> createRamlHandler("org/mule/module/apikit/raml-handler/failing-api.raml", keepRamlBaseUri, RAML));
  }

  private <A extends Exception, B> void assertException(Class<A> clazz, String message, Supplier<B> supplier) {
    try {
      supplier.get();
      fail(clazz.getName() + " was expected");
    } catch (Exception e) {
      assertTrue(clazz.isAssignableFrom(e.getClass()));
      assertTrue(e.getMessage().contains(message));
    }
  }

  private RamlHandler createRamlHandler(String ramlPath) {
    return createRamlHandler(ramlPath, true, AUTO);
  }

  private RamlHandler createRamlHandler(String ramlPath, boolean keepRamlBaseUri) {
    return createRamlHandler(ramlPath, keepRamlBaseUri, AUTO);
  }

  private RamlHandler createRamlHandler(String ramlPath, boolean keepRamlBaseUri, ParserType parser) {
    try {
      return new RamlHandler(ramlPath, keepRamlBaseUri, muleContext.getErrorTypeRepository(), parser);
    } catch (IOException e) {
      throw new RuntimeException("Error creating RamlHandler", e);
    }
  }
}
