/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.helpers.EventHelper;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.util.IOUtils;
import org.raml.parser.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

public class CharsetUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(CharsetUtils.class);

  /**
   * Tries to figure out the encoding of the request in the following order
   *  - checks if the content-type header includes the charset
   *  - detects the payload encoding using BOM, or tries to auto-detect it
   *  - return the mule message encoding
   *
   * @param event mule event
   * @param bytes payload byte array
   * @param logger where to log
   * @return payload encoding
   */
  public static String getEncoding(CoreEvent event, byte[] bytes, Logger logger) {

    String encoding = getHeaderCharset(event.getMessage());

    if (encoding == null) {
      encoding = StreamUtils.detectEncoding(bytes);
      logger.debug("Detected payload encoding: " + logEncoding(encoding));
      if (encoding == null) {
        encoding = getPayloadCharset(event.getMessage(), logger);

        if (encoding == null) {
          encoding = EventHelper.getEncoding(event).toString();
          logger.debug("Defaulting to mule message encoding: " + logEncoding(encoding));
        }
      }
    } else {
      logger.debug("Request Content-Type charset: " + logEncoding(encoding));
    }

    if (encoding.matches("(?i)UTF-16.+")) {
      encoding = "UTF-16";
    }
    return encoding;
  }

  /**
   * Tries to figure out the encoding of the request in the following order
   *  - checks if the content-type header includes the charset
   *  - detects the payload encoding using BOM, or tries to auto-detect it
   *  - return the mule message encoding
   *
   * @param message mule message
   * @param bytes payload byte array
   * @param logger where to log
   * @return payload encoding
   */
  public static String getEncoding(Message message, byte[] bytes, Logger logger) {
    String encoding = getHeaderCharset(message);
    if (encoding == null) {
      encoding = StreamUtils.detectEncoding(bytes);
      logger.debug("Detected payload encoding: " + logEncoding(encoding));
      if (encoding == null) {
        encoding = EventHelper.getEncoding(message).toString();
        logger.debug("Defaulting to mule message encoding: " + logEncoding(encoding));
      }
    }
    if (encoding.matches("(?i)UTF-16.+")) {
      encoding = "UTF-16";
    }
    return encoding;
  }

  /**
   * Tries to figure out the encoding of the request in the following order
   *  - checks if the content-type header includes the charset
   *  - Determine what type is payload
   *  - detects the payload encoding using BOM, or tries to auto-detect it
   *  - return the mule message encoding
   *
   * @param message mule message
   * @param input payload that would be instrospected
   * @param logger where to log
   * @return payload encoding
   */
  public static String getEncoding(Message message, Object input, Logger logger) throws IOException {
    String encoding = getHeaderCharset(message);
    byte[] bytes = null;

    if (encoding == null) {
      if (input instanceof InputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copyLarge((InputStream) input, baos);
        bytes = baos.toByteArray();
      } else if (input instanceof byte[]) {
        bytes = (byte[]) input;
      } else if (input instanceof String) {
        bytes = ((String) input).getBytes();
      }

      if (bytes == null) {
        return null;
      }
      encoding = StreamUtils.detectEncoding(bytes);
      logger.debug("Detected payload encoding: " + logEncoding(encoding));

      if (encoding == null) {
        encoding = EventHelper.getEncoding(message).toString();
        logger.debug("Defaulting to mule message encoding: " + logEncoding(encoding));
      }
    }
    if (encoding.matches("(?i)UTF-16.+")) {
      encoding = "UTF-16";
    }
    return encoding;
  }

  /**
   * Tries to figure out the encoding of an xml request in the following order
   *  - checks if the document has a content-type declaration
   *  - detects the payload encoding using BOM, or tries to auto-detect it
   *  - return the mule message encoding
   *
   * @param muleEvent mule event
   * @param payload xml payload as byte array
   * @param document xml parsed document
   * @param logger where to log
   * @return xml payload encoding
   */
  public static String getXmlEncoding(CoreEvent muleEvent, byte[] payload, Document document, Logger logger) {
    String encoding = document.getXmlEncoding();
    logger.debug("Xml declaration encoding: " + logEncoding(encoding));
    if (encoding == null) {
      encoding = StreamUtils.detectEncoding(payload);
      logger.debug("Detected payload encoding: " + logEncoding(encoding));
    }
    if (encoding == null) {
      encoding = EventHelper.getEncoding(muleEvent).toString();
      logger.debug("Defaulting to mule message encoding: " + logEncoding(encoding));
    }
    return encoding;
  }


  /**
   * Returns the charset specified by the content-type header or null if not specified
   *
   * @return header charset
   * @param message mule message
   */
  public static String getHeaderCharset(Message message) {
    return getCharset(AttributesHelper.getHeaderIgnoreCase(((HttpRequestAttributes) message.getAttributes().getValue()), "Content-Type"));
  }

  public static String getCharset(String contentType) {
    if (contentType == null)
      return null;

    MediaType mediaType = MediaType.parse(contentType);

    Optional<Charset> charset = mediaType.getCharset();
    return charset.map(Charset::name).orElse(null);
  }

  public static String getPayloadCharset(Message message, Logger logger) {
    Charset charset = message.getPayload().getDataType().getMediaType().getCharset().get();
    if (charset != null) {
      logger.debug("Request Payload charset: " + logEncoding(charset.toString()));
      return charset.toString();
    }
    return null;
  }

  /**
   * Removes BOM from byte array if present
   *
   * @param content byte array
   * @return BOM-less byte array
   */
  public static byte[] trimBom(byte[] content) {
    int bomSize = 0;
    if (content.length > 4) {
      // check for UTF_32BE and UTF_32LE BOMs
      if (content[0] == 0x00 && content[1] == 0x00 && content[2] == (byte) 0xFE && content[3] == (byte) 0xFF ||
          content[0] == (byte) 0xFF && content[1] == (byte) 0xFE && content[2] == 0x00 && content[3] == 0x00) {
        bomSize = 4;
      }
    }
    if (content.length > 3 && bomSize == 0) {
      // check for UTF-8 BOM
      if (content[0] == (byte) 0xEF && content[1] == (byte) 0xBB && content[2] == (byte) 0xBF) {
        bomSize = 3;
      }
    }
    if (content.length > 2 && bomSize == 0) {
      // check for UTF_16BE and UTF_16LE BOMs
      if (content[0] == (byte) 0xFE && content[1] == (byte) 0xFF || content[0] == (byte) 0xFF && content[1] == (byte) 0xFE) {
        bomSize = 2;
      }
    }

    if (bomSize > 0) {
      LOGGER.debug("Trimming {}-byte BOM", bomSize);
      int trimmedSize = content.length - bomSize;
      byte[] trimmedArray = new byte[trimmedSize];
      System.arraycopy(content, bomSize, trimmedArray, 0, trimmedSize);
      return trimmedArray;
    }
    return content;
  }


  private static String logEncoding(String encoding) {
    return encoding != null ? encoding : "not specified";
  }

}
