/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.jdom2.Element;

import org.mule.tools.apikit.output.GenerationModel;

import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

public class APIKitFlowScope implements Scope {
    private final Element flow;

    public APIKitFlowScope(GenerationModel flowEntry) {
        flow = new Element("flow", XMLNS_NAMESPACE.getNamespace());
        flow.setAttribute("name", flowEntry.getFlowName());

        if( flowEntry.getContentType() != null ) {
            Element setContentType = new Element("set-property", XMLNS_NAMESPACE.getNamespace());
            setContentType.setAttribute("propertyName", "Content-Type");
            setContentType.setAttribute("value", flowEntry.getContentType());
            flow.addContent(setContentType);
        }

        Element example = new Element("set-payload", XMLNS_NAMESPACE.getNamespace());
        example.setAttribute("value", flowEntry.getExample().trim());
        flow.addContent(example);
    }

    @Override
    public Element generate() {
        return flow;
    }
}
