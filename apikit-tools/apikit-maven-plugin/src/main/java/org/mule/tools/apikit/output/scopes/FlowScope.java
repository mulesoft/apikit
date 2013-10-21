package org.mule.tools.apikit.output.scopes;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;

import static org.mule.tools.apikit.output.MuleConfigGenerator.HTTP_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

public class FlowScope implements Scope {

    private final Element main;

    public FlowScope(Element mule, String exceptionStrategyRef, API api, String configRef) {
        main = new Element("flow", XMLNS_NAMESPACE.getNamespace());

        main.setAttribute("name", "main");

        Element httpInboundEndpoint = new Element("inbound-endpoint", HTTP_NAMESPACE.getNamespace());
        httpInboundEndpoint.setAttribute("address", api.getBaseUri());
        main.addContent(httpInboundEndpoint);

        Element restProcessor = new Element("router", APIKitTools.API_KIT_NAMESPACE.getNamespace());
        if(!StringUtils.isEmpty(configRef)) {
            restProcessor.setAttribute("config-ref", configRef);
        }
        main.addContent(restProcessor);

        Element exceptionStrategy = new Element("exception-strategy", XMLNS_NAMESPACE.getNamespace());
        exceptionStrategy.setAttribute("ref", exceptionStrategyRef);

        main.addContent(exceptionStrategy);

        mule.addContent(main);
    }

    @Override
    public Element generate() {
        return main;
    }
}
