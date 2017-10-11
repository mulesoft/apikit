/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import org.apache.commons.io.IOUtils;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.input.stream.RewindableInputStream;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static org.mule.module.apikit.CharsetUtils.trimBom;


public class PayloadHelper {

  protected static final Logger logger = LoggerFactory.getLogger(PayloadHelper.class);

  public static String getPayloadAsString(Object input, String charset) throws BadRequestException {
    final byte[] bytes;

    try {

      if (input instanceof CursorStreamProvider)
        bytes = IOUtils.toByteArray(((CursorStreamProvider) input).openCursor());
      else if (input instanceof RewindableInputStream) {
        final RewindableInputStream rewindable = (RewindableInputStream) input;
        bytes = IOUtils.toByteArray(rewindable);
        rewindable.rewind();
      } else if (input instanceof InputStream)
        bytes = IOUtils.toByteArray((InputStream) input);
      else if (input instanceof String)
        bytes = ((String) input).getBytes();
      else if (input instanceof byte[])
        bytes = (byte[]) input;
      else if (input != null)
        throw new BadRequestException("Don't know how to parse " + input.getClass().getName());
      else
        throw new BadRequestException("Don't know how to parse payload");

      return IOUtils.toString(trimBom(bytes), charset);

    } catch (IOException e) {
      throw new BadRequestException("Error processing request: " + e.getMessage());
    }
  }

}
