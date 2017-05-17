/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.jdom2.CDATA;
import org.jdom2.Element;

import org.mule.tools.apikit.output.GenerationModel;
import org.mule.weave.v2.module.reader.StringSourceProvider;
import org.mule.weave.v2.runtime.utils.WeaveSimpleRunner;

import static org.mule.tools.apikit.output.MuleConfigGenerator.EE_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XSI_NAMESPACE;

public class APIKitFlowScope implements Scope {
    private final Element flow;

    private static final String LOGGER_ATTRIBUTE_LEVEL = "level";
    private static final String LOGGER_ATTRIBUTE_MESSAGE = "message";
    private static final String LOGGER_ATTRIBUTE_LEVEL_VALUE = "INFO";

    private static final String DEFAULT_EXAMPLE_CONTENT_TYPE = "application/java";
    private static final String APPLICATION_DATAWEAVE = "application/dw";
    private static final String DW_INPUT_TYPE = "payload";

    public APIKitFlowScope(GenerationModel flowEntry) {
        flow = new Element("flow", XMLNS_NAMESPACE.getNamespace());
        flow.setAttribute("name", flowEntry.getFlowName());

        if (flowEntry.getExampleWrapper() == null)
        {
            Element logger = new Element("logger", XMLNS_NAMESPACE.getNamespace());
            logger.setAttribute(LOGGER_ATTRIBUTE_LEVEL, LOGGER_ATTRIBUTE_LEVEL_VALUE);
            logger.setAttribute(LOGGER_ATTRIBUTE_MESSAGE, flow.getAttribute("name").getValue());
            flow.addContent(logger);
        }
        else
        {
            Element transform = new Element("transform", EE_NAMESPACE.getNamespace());
            Element setPayload = new Element("set-payload", EE_NAMESPACE.getNamespace());
            CDATA cdataSection = new CDATA(generateTransformText(flowEntry.getContentType(), flowEntry.getExampleWrapper()));
            setPayload.addContent(cdataSection);
            transform.addNamespaceDeclaration(EE_NAMESPACE.getNamespace());
            transform.setAttribute("schemaLocation", EE_NAMESPACE.getNamespace().getURI() + " " + EE_NAMESPACE.getLocation(), XSI_NAMESPACE.getNamespace());
            transform.addContent(setPayload);
            flow.addContent(transform);
        }
    }

    private String generateTransformText(String contentType, String example)
    {
        String transformContentType = contentType;
        if (contentType == null)
        {
            transformContentType = DEFAULT_EXAMPLE_CONTENT_TYPE;
        }

        WeaveSimpleRunner runner = new WeaveSimpleRunner();
        runner.addInput(DW_INPUT_TYPE, transformContentType, new StringSourceProvider(example.trim()));
        runner.setOutputType(APPLICATION_DATAWEAVE);
        String result = runner.execute(DW_INPUT_TYPE).toString();

        return "            %output "+ transformContentType +"\n" +
               "             ---\n" +
               "             " + result + "\n";
    }
    @Override
    public Element generate() {
        return flow;
    }
}
