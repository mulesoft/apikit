/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.runtime.core.api.exception.TypedException;

public class PayloadHelperTestCase {

  @Test
  public void validStreamPayload() throws TypedException, BadRequestException {
    InputStream is = new ByteArrayInputStream("{ \"name\": \"Major League Soccer\" }".getBytes());

    assertEquals("{ \"name\": \"Major League Soccer\" }", PayloadHelper.getPayloadAsString(is, "UTF-8", true));
  }

  @Test
  public void validStringPayload() throws TypedException, BadRequestException {
    String payload = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>";

    assertEquals(payload, PayloadHelper.getPayloadAsString(payload, "UTF-8", true));
  }

  @Test
  public void validInputStreamPayload() throws TypedException, BadRequestException {
    String payloadString = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>";

    InputStream payloadStream = new ByteArrayInputStream(payloadString.getBytes(StandardCharsets.UTF_8));
    assertEquals(payloadString, PayloadHelper.getPayloadAsString(payloadStream, "UTF-8", true));
    //re-reading payload to check if it was consumed
    payloadStream = new ByteArrayInputStream(payloadString.getBytes(StandardCharsets.UTF_8));
    assertEquals(payloadString, PayloadHelper.getPayloadAsString(payloadStream, "UTF-8", true));
  }

  @Test
  public void validBytesPayload() throws TypedException, BadRequestException {
    byte[] payload = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>".getBytes();

    assertEquals("<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>",
                 PayloadHelper.getPayloadAsString(payload, "UTF-8", true));
  }

  @Test(expected = TypedException.class)
  public void nullPayload() throws TypedException, BadRequestException {

    PayloadHelper.getPayloadAsString(null, "UTF-8", true);

  }
}
