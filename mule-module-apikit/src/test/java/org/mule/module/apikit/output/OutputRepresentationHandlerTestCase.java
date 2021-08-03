/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.output;

import java.util.Arrays;
import java.util.HashMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mule.module.apikit.HttpProtocolAdapter;
import org.mule.module.apikit.OutputRepresentationHandler;
import org.mule.module.apikit.RestContentTypeParser;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IResponse;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestContentTypeParser.class)
public class OutputRepresentationHandlerTestCase {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Test
  public void mimeTypeUpperCase() throws Exception {
    IAction action = mock(IAction.class);
    when(action.getResponses()).thenReturn(new HashMap<String, IResponse>());

    HttpProtocolAdapter protocolAdapter = mock(HttpProtocolAdapter.class);
    when(protocolAdapter.getAcceptableResponseMediaTypes()).thenReturn("*/*");

    OutputRepresentationHandler handler = new OutputRepresentationHandler(protocolAdapter,true);
    String mimeType = handler.negotiateOutputRepresentation(action, Arrays.asList("APPLICATION/JSON"));
    assertEquals(mimeType, "APPLICATION/JSON");
  }

  @Test
  public void mimeTypeWithAttributes() throws Exception {
    IAction action = mock(IAction.class);
    when(action.getResponses()).thenReturn(new HashMap<String, IResponse>());

    HttpProtocolAdapter protocolAdapter = mock(HttpProtocolAdapter.class);
    when(protocolAdapter.getAcceptableResponseMediaTypes()).thenReturn("*/*");

    OutputRepresentationHandler handler = new OutputRepresentationHandler(protocolAdapter,true);
    String mimeType = handler.negotiateOutputRepresentation(action, Arrays.asList("application/custom+json;version=1"));
    assertEquals(mimeType, "application/custom+json;version=1");
  }

  @Test
  public void actionIsNull() throws Exception {
    HttpProtocolAdapter protocolAdapter = mock(HttpProtocolAdapter.class);
    OutputRepresentationHandler handler = new OutputRepresentationHandler(protocolAdapter, true);
    assertNull(handler.negotiateOutputRepresentation(null, Arrays.asList("APPLICATION/JSON")));
  }

  @Test
  public void actionResponsesAreNull() throws Exception {
    IAction action = mock(IAction.class);
    when(action.getResponses()).thenReturn(null);

    HttpProtocolAdapter protocolAdapter = mock(HttpProtocolAdapter.class);
    OutputRepresentationHandler handler = new OutputRepresentationHandler(protocolAdapter, true);

    assertNull(handler.negotiateOutputRepresentation(action, Arrays.asList("APPLICATION/JSON")));
  }

  @Test
  public void bestMatchIsNull() throws Exception {
    expected.expect(NotAcceptableException.class);
    IAction action = mock(IAction.class);
    when(action.getResponses()).thenReturn(new HashMap<String, IResponse>());

    HttpProtocolAdapter protocolAdapter = mock(HttpProtocolAdapter.class);
    when(protocolAdapter.getAcceptableResponseMediaTypes()).thenReturn("*/*");

    PowerMockito.mockStatic(RestContentTypeParser.class);
    when(RestContentTypeParser.bestMatch(anyList(), any(String.class))).thenReturn(null);

    new OutputRepresentationHandler(protocolAdapter, true).negotiateOutputRepresentation(action, Arrays.asList("APPLICATION/JSON"));
  }

  @Test
  public void bestMatchIsNullNotThrowExpection() throws Exception {
    IAction action = mock(IAction.class);
    when(action.getResponses()).thenReturn(new HashMap<String, IResponse>());

    HttpProtocolAdapter protocolAdapter = mock(HttpProtocolAdapter.class);
    when(protocolAdapter.getAcceptableResponseMediaTypes()).thenReturn("*/*");

    PowerMockito.mockStatic(RestContentTypeParser.class);
    when(RestContentTypeParser.bestMatch(anyList(), any(String.class))).thenReturn(null);

    assertNull(new OutputRepresentationHandler(protocolAdapter, false).negotiateOutputRepresentation(action, Arrays.asList("APPLICATION/JSON")));
  }
}
