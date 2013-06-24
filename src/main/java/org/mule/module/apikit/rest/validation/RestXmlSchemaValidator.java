/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.validation;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.validation.cache.XmlSchemaCache;
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

import apikit2.exception.BadRequestException;
import heaven.model.Heaven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RestXmlSchemaValidator extends AbstractRestSchemaValidator
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public RestXmlSchemaValidator(MuleContext muleContext)
    {
        super(muleContext);
    }

    @Override
    public void validate(String schemaLocation, MuleEvent muleEvent) throws InvalidInputException
    {
        try
        {
            validate(schemaLocation, muleEvent, null);
        }
        catch (BadRequestException badRequestException)
        {
            throw new InvalidInputException(badRequestException);
        }
    }

    @Override
    public void validate(String schemaPath, MuleEvent muleEvent, Heaven api) throws BadRequestException
    {
        try
        {

            Document data;
            Object input = muleEvent.getMessage().getPayload();
            if (input instanceof String)
            {
                data = loadDocument(IOUtils.toInputStream((String) input));
            }
            else if (input instanceof InputStream)
            {
                data = loadDocument((InputStream) input);
            }
            else if (input instanceof byte[])
            {
                data = loadDocument(new ByteArrayInputStream((byte[]) input));
            }
            else
            {
                throw new InvalidInputException("Don't know how to parse " + input.getClass().getName());
            }

            Schema schema = XmlSchemaCache.getXmlSchemaCache(muleContext, api).get(schemaPath);
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
}
