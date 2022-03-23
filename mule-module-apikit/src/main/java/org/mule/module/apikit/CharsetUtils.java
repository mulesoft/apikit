/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.transport.http.HttpConstants;

import java.nio.charset.Charset;
import org.raml.parser.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class CharsetUtils
{

    private static final Logger LOGGER = LoggerFactory.getLogger(CharsetUtils.class);

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
    public static String getEncoding(MuleMessage message, byte[] bytes, Logger logger)
    {
        String encoding = getHeaderCharset(message, logger);
        if (encoding == null)
        {
            encoding = detectEncodingOrDefault(bytes);
            logger.debug("Detected payload encoding: " + logEncoding(encoding));
            if (encoding == null)
            {
                encoding = message.getEncoding();
                logger.debug("Defaulting to mule message encoding: " + logEncoding(encoding));
            }
        }
        if (encoding.matches("(?i)UTF-16.+"))
        {
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
    public static String getXmlEncoding(MuleEvent muleEvent, byte[] payload, Document document, Logger logger)
    {
        String encoding = document.getXmlEncoding();
        logger.debug("Xml declaration encoding: " + logEncoding(encoding));
        if (encoding == null)
        {
            encoding = detectEncodingOrDefault(payload);
            logger.debug("Detected payload encoding: " + logEncoding(encoding));
        }
        if (encoding == null)
        {
            encoding = muleEvent.getEncoding();
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
    public static String getHeaderCharset(MuleMessage message, Logger logger)
    {
        String charset = null;
        String contentType = message.getInboundProperty(HttpConstants.HEADER_CONTENT_TYPE, "application/xml");
        if (contentType.contains("charset="))
        {
            charset = message.getEncoding();
            logger.debug("Request Content-Type charset: " + logEncoding(charset));
        }
        return charset;
    }


    /**
     * Removes BOM from byte array if present
     *
     * @param content byte array
     * @return BOM-less byte array
     */
    public static byte[] trimBom(byte[] content)
    {
        int bomSize = 0;
        if (content.length > 4)
        {
            // check for UTF_32BE and UTF_32LE BOMs
            if (content[0] == 0x00 && content[1] == 0x00 && content[2] == (byte) 0xFE && content[3] == (byte) 0xFF ||
                content[0] == (byte) 0xFF && content[1] == (byte) 0xFE && content[2] == 0x00 && content[3] == 0x00)
            {
                bomSize = 4;
            }
        }
        if (content.length > 3 && bomSize == 0)
        {
            // check for UTF-8 BOM
            if (content[0] == (byte) 0xEF && content[1] == (byte) 0xBB && content[2] == (byte) 0xBF)
            {
                bomSize = 3;
            }
        }
        if (content.length > 2 && bomSize == 0)
        {
            // check for UTF_16BE and UTF_16LE BOMs
            if (content[0] == (byte) 0xFE && content[1] == (byte) 0xFF || content[0] == (byte) 0xFF && content[1] == (byte) 0xFE)
            {
                bomSize = 2;
            }
        }

        if (bomSize > 0)
        {
            LOGGER.debug("Trimming {}-byte BOM", bomSize);
            int trimmedSize = content.length - bomSize;
            byte[] trimmedArray = new byte[trimmedSize];
            System.arraycopy(content, bomSize, trimmedArray, 0, trimmedSize);
            return trimmedArray;
        }
        return content;
    }


    private static String logEncoding(String encoding)
    {
        return encoding != null ? encoding : "not specified";
    }

    /**
     * <p>Given an array of bytes tries to detect the text encoding of them
     * unless <i>apikit.disableEncodingGuessing</i> is set to true.</p>
     *
     * <p>If <i>apikit.disableEncodingGuessing</i> is set to true then the
     * encoding name of {@link Charset#defaultCharset()} is returned.
     *
     * @param bytes The array of bytes to examine
     * @return The name of the detected encoding
     */
    public static String detectEncodingOrDefault(byte[] bytes)
    {
        boolean shouldGuessEncoding = !Boolean.parseBoolean(System.getProperty("apikit.disableEncodingGuessing"));
        return shouldGuessEncoding ? StreamUtils.detectEncoding(bytes) : Charset.defaultCharset().toString();
    }
}
