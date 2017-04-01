/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v1;

import org.mule.module.apikit.EventHelper;
import org.mule.module.apikit.MessageHelper;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.DataTypeBuilder;
import org.mule.runtime.core.api.config.MuleProperties;

import com.google.common.cache.LoadingCache;

import org.apache.commons.lang.math.NumberUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RestXmlSchemaValidator
{
    protected static int bufferSize = NumberUtils.toInt(System.getProperty(MuleProperties.MULE_STREAMING_BUFFER_SIZE), 4 * 1024);

    public static final String EXTERNAL_ENTITIES_PROPERTY = "raml.xml.expandExternalEntities";
    public static final String EXPAND_ENTITIES_PROPERTY = "raml.xml.expandInternalEntities";

    private static final Boolean externalEntities =
            Boolean.parseBoolean(System.getProperty(EXTERNAL_ENTITIES_PROPERTY, "false"));
    private static final Boolean expandEntities =
            Boolean.parseBoolean(System.getProperty(EXPAND_ENTITIES_PROPERTY, "false"));
    protected static final Logger logger = LoggerFactory.getLogger(RestXmlSchemaValidator.class);

    private LoadingCache<String, Schema> schemaCache;

    public RestXmlSchemaValidator(LoadingCache<String, Schema> schemaCache)
    {
        this.schemaCache = schemaCache;
    }

    public Message validate(String schemaPath, Message message) throws BadRequestException
    {
        Message newMessage = message;
        try
        {

            Document data;
            Object input = message.getPayload().getValue();
            Charset messageEncoding = EventHelper.getEncoding(message);
            if (input instanceof InputStream)
            {
                logger.debug("transforming payload to perform XSD validation");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try
                {
                    copyLarge((InputStream) input, baos);
                }
                finally
                {
                    IOUtils.closeQuietly((InputStream) input);
                }

                DataType dataType = message.getPayload().getDataType();

                DataTypeBuilder sourceDataTypeBuilder = DataType.builder();
                sourceDataTypeBuilder.type(message.getPayload().getClass());
                sourceDataTypeBuilder.mediaType(dataType.getMediaType());
                sourceDataTypeBuilder.charset(messageEncoding);
                DataType sourceDataType = sourceDataTypeBuilder.build();//DataTypeFactory.create(event.getMessage().getPayload().getClass(), msgMimeType);
                newMessage = MessageHelper.setPayload(message, new ByteArrayInputStream(baos.toByteArray()), sourceDataType.getMediaType());
                data = loadDocument(new ByteArrayInputStream(baos.toByteArray()), messageEncoding.toString());
            }
            else if (input instanceof String)
            {
                data = loadDocument(new StringReader((String) input));
            }
            else if (input instanceof byte[])
            {
                data = loadDocument(new ByteArrayInputStream((byte[]) input), messageEncoding.toString());
            }
            else
            {
                throw new BadRequestException("Don't know how to parse " + input.getClass().getName());
            }

            Schema schema = schemaCache.get(schemaPath);
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(data.getDocumentElement()));
        }
        catch (Exception e)
        {
            logger.info("Schema validation failed: " + e.getMessage());
            throw new BadRequestException(e);
        }
        return newMessage;
    }

    private static Document loadDocument(InputStream stream, String charset) throws IOException
    {
        if (charset == null)
        {
            return loadDocument(new InputSource(stream));
        }
        return loadDocument(new InputSource(new InputStreamReader(stream, charset)));
    }

    private static Document loadDocument(Reader reader) throws IOException
    {
        return loadDocument(new InputSource(reader));
    }

    /**
     * Loads the document from the <code>content</code>.
     *
     * @param source the content to load
     * @return the {@link Document} represents the DOM of the content
     * @throws IOException
     */
    private static Document loadDocument(InputSource source) throws IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        setFeatures(factory);
        factory.setNamespaceAware(true);
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            //Setting error handler to null to avoid logs generated by the parser.
            builder.setErrorHandler(null);
            return builder.parse(source);
        }
        catch (ParserConfigurationException e)
        {
            throw new IOException("An internal operation failed.", e);
        }
        catch (SAXException e)
        {
            throw new IOException("An internal operation failed.", e);
        }
    }

    /*
     * Prevent XXE attacks
     * <code>https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing</code>
     */
    private static void setFeatures(DocumentBuilderFactory dbf)
    {
        String feature = null;
        try
        {

            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            //feature  = "http://apache.org/xml/features/disallow-doctype-decl";
            //dbf.setFeature(feature, true);

            // If you can't completely disable DTDs, then at least do the following:
            feature = "http://xml.org/sax/features/external-general-entities";
            dbf.setFeature(feature, externalEntities);

            feature = "http://xml.org/sax/features/external-parameter-entities";
            dbf.setFeature(feature, externalEntities);

            feature = "http://apache.org/xml/features/disallow-doctype-decl";
            dbf.setFeature(feature, !expandEntities);

            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks" (see reference below)
            dbf.setXIncludeAware(expandEntities);
            dbf.setExpandEntityReferences(expandEntities);

        }
        catch (ParserConfigurationException e)
        {
            logger.info("ParserConfigurationException was thrown. The feature '" + feature +
                        "' is probably not supported by your XML processor.");
        }
    }

    /**
     * Re-implement copy method to allow buffer size to be configured. This won't impact all methods because there is no
     * polymorphism for static methods, but rather just direct use of these two methods.
     */
    public static long copyLarge(InputStream input, OutputStream output) throws IOException {

        byte[] buffer = new byte[bufferSize];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Re-implement copy method to allow buffer size to be configured. This won't impact all methods because there is no
     * polymorphism for static methods, but rather just direct use of these two methods.
     */
    public static long copyLarge(Reader input, Writer output) throws IOException {
        char[] buffer = new char[bufferSize];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    //public static int toInt(Object obj) {
    //    if (obj == null) {
    //        throw new IllegalArgumentException("Unable to convert null object to int");
    //    } else if (obj instanceof String) {
    //        return toInt((String) obj);
    //    } else if (obj instanceof Number) {
    //        return ((Number) obj).intValue();
    //    } else {
    //        throw new IllegalArgumentException("Unable to convert object of type: " + obj.getClass().getName() + " to int.");
    //    }
    //}

}
