package org.mule.tools.apikit.output.scopes;


import org.apache.commons.lang.StringEscapeUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.output.GenerationModel;

import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

public class APIKitFlowScope implements Scope {
    private final Element flow;

    public APIKitFlowScope(GenerationModel flowEntry, Element mule) {
        flow = new Element("flow", XMLNS_NAMESPACE.getNamespace());
        flow.setAttribute("name", flowEntry.getFlowName());
        // Example
        Element example = new Element("set-payload", XMLNS_NAMESPACE.getNamespace());
        example.setAttribute("value", StringEscapeUtils.escapeJava(flowEntry.getExample()));

        flow.addContent(example);
        mule.addContent("\n    ");
        mule.addContent(flow);
    }

    @Override
    public Element generate() {
        return flow;
    }
}
