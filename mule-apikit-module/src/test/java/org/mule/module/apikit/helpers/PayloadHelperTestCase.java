/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import static org.junit.Assert.assertEquals;
import static org.mule.module.apikit.helpers.PayloadHelper.getPayloadAsString;

import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.input.stream.RewindableInputStream;
import org.mule.runtime.api.exception.TypedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class PayloadHelperTestCase {

  @Test
  public void validStreamPayload() throws TypedException, BadRequestException {
    InputStream is = new ByteArrayInputStream("{ \"name\": \"Major League Soccer\" }".getBytes());

    assertEquals("{ \"name\": \"Major League Soccer\" }", getPayloadAsString(is, "UTF-8"));
  }

  @Test
  public void validStringPayload() throws TypedException, BadRequestException {
    String payload = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>";

    assertEquals(payload, getPayloadAsString(payload, "UTF-8"));
  }

  @Test
  public void validInputStreamPayload() throws TypedException, BadRequestException {
    String payloadString = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>";

    InputStream payloadStream = new ByteArrayInputStream(payloadString.getBytes(StandardCharsets.UTF_8));
    assertEquals(payloadString, getPayloadAsString(payloadStream, "UTF-8"));
  }

  @Test
  public void validRewindableInputStreamPayload() throws TypedException, BadRequestException {
    String payloadString = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>";

    final InputStream payloadStream = new ByteArrayInputStream(payloadString.getBytes(StandardCharsets.UTF_8));
    final RewindableInputStream rewindable = new RewindableInputStream(payloadStream);

    assertEquals(payloadString, getPayloadAsString(rewindable, "UTF-8"));
    //re-reading payload to check if it was consumed
    assertEquals(payloadString, getPayloadAsString(rewindable, "UTF-8"));
  }

  @Test
  public void validBytesPayload() throws TypedException, BadRequestException {
    byte[] payload = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>".getBytes();

    assertEquals("<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>",
                 getPayloadAsString(payload, "UTF-8"));
  }

  @Test(expected = BadRequestException.class)
  public void nullPayload() throws TypedException, BadRequestException {

    getPayloadAsString(null, "UTF-8");

  }

  @Test
  public void validStreamPayloadWithBOM() throws TypedException, BadRequestException {
    final String utf8BOM = "\uFEFF";
    final String payload = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><greeting>hello</greeting></league>";

    assertEquals(payload, getPayloadAsString(utf8BOM + payload, "UTF-8"));
  }
}
