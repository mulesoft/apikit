/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.jdom2.CDATA;
import org.jdom2.Element;

import org.mule.tools.apikit.misc.ExampleUtils;
import org.mule.tools.apikit.output.GenerationModel;

import static org.mule.tools.apikit.output.MuleConfigGenerator.EE_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XSI_NAMESPACE;

public class APIKitFlowScope implements Scope {
    private final Element flow;

    private static final String LOGGER_ATTRIBUTE_LEVEL = "level";
    private static final String LOGGER_ATTRIBUTE_MESSAGE = "message";
    private static final String LOGGER_ATTRIBUTE_LEVEL_VALUE = "INFO";

    public APIKitFlowScope(GenerationModel flowEntry) {
        this(flowEntry, true);
    }

    public APIKitFlowScope(GenerationModel flowEntry, boolean isMuleEE) {
        flow = new Element("flow", XMLNS_NAMESPACE.getNamespace());
        flow.setAttribute("name", flowEntry.getFlowName());
        flow.addContent(generateFlowContent(flowEntry, isMuleEE));
    }

    private Element generateFlowContent(GenerationModel flowEntry, boolean isMuleEE)
    {
        if (isMuleEE && flowEntry.getExampleWrapper() != null)
        {
            try
            {
                return generateTransform(flowEntry);
            }
            catch (Exception e)
            {
                return generateLogger(flowEntry.getFlowName());
            }
        }
        else
        {
            return generateLogger(flowEntry.getFlowName());
        }
    }

    private Element generateTransform(GenerationModel flowEntry) {
        Element transform = new Element("transform", EE_NAMESPACE.getNamespace());
        Element setPayload = new Element("set-payload", EE_NAMESPACE.getNamespace());
        Element message = new Element("message", EE_NAMESPACE.getNamespace());
        CDATA cdataSection = new CDATA(generateTransformTextForExample(flowEntry.getExampleWrapper().trim()));
        setPayload.addContent(cdataSection);
        message.setContent(setPayload);
        transform.addNamespaceDeclaration(EE_NAMESPACE.getNamespace());
        transform.setAttribute("schemaLocation", EE_NAMESPACE.getNamespace().getURI() + " " + EE_NAMESPACE.getLocation(), XSI_NAMESPACE.getNamespace());
        transform.addContent(message);
        return transform;
    }

    private Element generateLogger(String message)
    {
        Element logger = new Element("logger", XMLNS_NAMESPACE.getNamespace());
        logger.setAttribute(LOGGER_ATTRIBUTE_LEVEL, LOGGER_ATTRIBUTE_LEVEL_VALUE);
        logger.setAttribute(LOGGER_ATTRIBUTE_MESSAGE, message);
        return logger;
    }

    private String generateTransformTextForExample(String example)
    {
        return ExampleUtils.getDataWeaveExpressionText(example);
    }

    @Override
    public Element generate() {
        return flow;
    }
}
