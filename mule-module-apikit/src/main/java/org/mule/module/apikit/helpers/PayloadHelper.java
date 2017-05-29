/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import static org.mule.module.apikit.CharsetUtils.trimBom;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.input.stream.RewindableInputStream;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PayloadHelper {
  protected static final Logger logger = LoggerFactory.getLogger(PayloadHelper.class);

  public static String getPayloadAsString(Object input, String charset, boolean trimBom) throws BadRequestException
  {
    if (input instanceof CursorStreamProvider)
    {
      return IOUtils.toString(((CursorStreamProvider) input).openCursor());
    }

    if (input instanceof InputStream)
    {
      logger.debug("transforming payload to perform Schema validation");
      RewindableInputStream rewindableInputStream = new RewindableInputStream((InputStream) input);
      input = IOUtils.toString(rewindableInputStream);
      rewindableInputStream.rewind();

    }
    else if (input instanceof byte[])
    {
      try
      {
        input = byteArrayToString((byte[]) input, charset, trimBom);
      }
      catch (IOException e)
      {
        throw ApikitErrorTypes.BAD_REQUEST.throwErrorType("Error processing request: " + e.getMessage());
      }
    }
    else if (input instanceof String)
    {
      // already in the right format
    }
    else
    {
      String errorMessage = "Don't know how to parse payload";
      if (input != null)
      {
        errorMessage = "Don't know how to parse " + input.getClass().getName();
      }
      throw ApikitErrorTypes.BAD_REQUEST.throwErrorType(errorMessage);

    }
    return (String) input;
  }

  private static String byteArrayToString(byte[] bytes, String charset, boolean trimBom) throws IOException
  {
    String result;
    if (trimBom)
    {
      result = IOUtils.toString(new ByteArrayInputStream(trimBom(bytes)), charset);
    }
    else
    {
      result = IOUtils.toString(bytes, charset);
    }
    return result;
  }

}
