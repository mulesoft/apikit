/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.transformer.DataType;
import org.mule.module.apikit.validation.cache.XmlSchemaCache;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.mule.module.apikit.exception.BadRequestException;
import org.raml.model.Raml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RestXmlSchemaValidator extends AbstractRestSchemaValidator
{

    protected static final Logger logger = LoggerFactory.getLogger(RestXmlSchemaValidator.class);

    public RestXmlSchemaValidator(MuleContext muleContext)
    {
        super(muleContext);
    }

    @Override
    public void validate(String configId, String schemaPath, MuleEvent muleEvent, Raml api) throws BadRequestException
    {
        try
        {

            Document data;
            Object input = muleEvent.getMessage().getPayload();
            if (input instanceof InputStream)
            {
                input = IOUtils.toString((InputStream) input, muleEvent.getMessage().getEncoding());
                logger.debug("transforming payload to perform XSD validation");
                DataType<String> dataType = DataTypeFactory.create(String.class, muleEvent.getMessage().getDataType().getMimeType());
                muleEvent.getMessage().setPayload(input, dataType);
            }
            if (input instanceof String)
            {
                data = loadDocument(IOUtils.toInputStream((String) input));
            }
            else if (input instanceof byte[])
            {
                data = loadDocument(new ByteArrayInputStream((byte[]) input));
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

    /**
     * Loads the document from the <code>content</code>.
     *
     * @param inputStream the content to load
     * @return the {@link org.w3c.dom.Document} represents the DOM of the content
     * @throws java.io.IOException
     */
    public static Document loadDocument(InputStream inputStream) throws IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        setFeatures(factory);
        factory.setNamespaceAware(true);
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(inputStream));
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
            dbf.setFeature(feature, false);

            feature = "http://xml.org/sax/features/external-parameter-entities";
            dbf.setFeature(feature, false);

            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks" (see reference below)
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

        }
        catch (ParserConfigurationException e)
        {
            logger.info("ParserConfigurationException was thrown. The feature '" + feature +
                        "' is probably not supported by your XML processor.");
        }
    }
}
