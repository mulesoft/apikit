package org.mule.tools.apikit.output.scopes;


import org.jdom2.Element;

import org.mule.tools.apikit.output.GenerationModel;

import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

public class APIKitFlowScope implements Scope {
    private final Element flow;

    public APIKitFlowScope(GenerationModel flowEntry, Element mule) {
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
        mule.addContent(flow);
    }

    @Override
    public Element generate() {
        return flow;
    }
}
