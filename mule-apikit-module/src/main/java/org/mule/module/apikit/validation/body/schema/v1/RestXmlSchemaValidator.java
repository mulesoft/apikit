/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v1;

import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.IRestSchemaValidatorStrategy;
import org.mule.runtime.core.api.config.MuleProperties;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RestXmlSchemaValidator implements IRestSchemaValidatorStrategy
{
    protected static int bufferSize = NumberUtils.toInt(System.getProperty(MuleProperties.MULE_STREAMING_BUFFER_SIZE), 4 * 1024);

    public static final String EXTERNAL_ENTITIES_PROPERTY = "raml.xml.expandExternalEntities";
    public static final String EXPAND_ENTITIES_PROPERTY = "raml.xml.expandInternalEntities";

    private static final Boolean externalEntities =
            Boolean.parseBoolean(System.getProperty(EXTERNAL_ENTITIES_PROPERTY, "false"));
    private static final Boolean expandEntities =
            Boolean.parseBoolean(System.getProperty(EXPAND_ENTITIES_PROPERTY, "false"));
    protected static final Logger logger = LoggerFactory.getLogger(RestXmlSchemaValidator.class);

    private Schema schema;

    public RestXmlSchemaValidator(Schema schemaCache)
    {
        this.schema = schemaCache;
    }

    @Override
    public void validate(String payload) throws BadRequestException {
        try {
            Document data = loadDocument(new StringReader(payload));

            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(data.getDocumentElement()));

        } catch (IOException|SAXException e) {
            logger.info("Schema validation failed: " + e.getMessage());
            throw ApikitErrorTypes.throwErrorType(new BadRequestException(e));
        }

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
}
