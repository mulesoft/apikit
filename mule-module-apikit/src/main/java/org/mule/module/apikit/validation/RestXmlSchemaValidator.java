/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import static org.mule.module.apikit.CharsetUtils.getHeaderCharset;
import static org.mule.module.apikit.CharsetUtils.getXmlEncoding;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.transformer.DataType;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.validation.cache.XmlSchemaCache;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RestXmlSchemaValidator extends AbstractRestSchemaValidator
{
    public static final String EXTERNAL_ENTITIES_PROPERTY = "raml.xml.expandExternalEntities";
    public static final String EXPAND_ENTITIES_PROPERTY = "raml.xml.expandInternalEntities";

    private static final Boolean externalEntities =
            Boolean.parseBoolean(System.getProperty(EXTERNAL_ENTITIES_PROPERTY, "false"));
    private static final Boolean expandEntities =
            Boolean.parseBoolean(System.getProperty(EXPAND_ENTITIES_PROPERTY, "false"));
    protected static final Logger logger = LoggerFactory.getLogger(RestXmlSchemaValidator.class);

    public RestXmlSchemaValidator(MuleContext muleContext)
    {
        super(muleContext);
    }

    @Override
    public void validate(String configId, String schemaPath, MuleEvent muleEvent, IRaml api) throws BadRequestException
    {
        try
        {

            Document data;
            Object input = muleEvent.getMessage().getPayload();
            String charset = getHeaderCharset(muleEvent.getMessage(), logger);
            if (input instanceof InputStream)
            {
                logger.debug("Transforming payload to perform XSD validation");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try
                {
                    IOUtils.copyLarge((InputStream) input, baos);
                }
                finally
                {
                    IOUtils.closeQuietly((InputStream) input);
                }
                data = loadDocument(new ByteArrayInputStream(baos.toByteArray()), charset);

                // update payload and set encoding
                String encoding = charset != null ? charset : getXmlEncoding(muleEvent, baos.toByteArray(), data, logger);
                DataType<?> dataType = getDataType(muleEvent, ByteArrayInputStream.class, encoding);
                muleEvent.getMessage().setPayload(new ByteArrayInputStream(baos.toByteArray()), dataType);
            }
            else if (input instanceof String)
            {
                data = loadDocument(new StringReader((String) input));
            }
            else if (input instanceof byte[])
            {
                data = loadDocument(new ByteArrayInputStream((byte[]) input), charset);

                // update message encoding
                String encoding = charset != null ? charset : getXmlEncoding(muleEvent, (byte[]) input, data, logger);
                muleEvent.getMessage().setPayload(input, getDataType(muleEvent, encoding));
            }
            else
            {
                throw new BadRequestException("Don't know how to parse " + input.getClass().getName());
            }

            Schema schema = XmlSchemaCache.getXmlSchemaCache(muleContext, configId, api).get(schemaPath);
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(data.getDocumentElement()));
        }
        catch (Exception e)
        {
            logger.info("Schema validation failed: " + e.getMessage());
            throw new BadRequestException(e);
        }
    }

    private DataType<?> getDataType(MuleEvent muleEvent, String encoding)
    {
        return getDataType(muleEvent, muleEvent.getMessage().getDataType().getType(), encoding);
    }

    private DataType<?> getDataType(MuleEvent muleEvent, Class<?> type, String encoding)
    {
        DataType<?> messageDataType = muleEvent.getMessage().getDataType();
        DataType<?> dataType = DataTypeFactory.create(type, messageDataType.getMimeType());
        dataType.setEncoding(encoding);
        return dataType;
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
     * @return the {@link org.w3c.dom.Document} represents the DOM of the content
     * @throws java.io.IOException
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
}
