/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.apikit.output.scopes;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;

import static org.mule.tools.apikit.output.MuleConfigGenerator.HTTP_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

public class ConsoleFlowScope implements Scope {

    private final Element consoleFlow;


    public ConsoleFlowScope(Element mule, API api, String configRef, String httpListenerConfigRef){

        consoleFlow = new Element("flow", XMLNS_NAMESPACE.getNamespace());

        consoleFlow.setAttribute("name", api.getId() + "-" + "console");

        if (httpListenerConfigRef != null)
        {
            Element httpListener = new Element("listener", HTTP_NAMESPACE.getNamespace());
            httpListener.setAttribute("config-ref", httpListenerConfigRef);
            httpListener.setAttribute("path", API.DEFAULT_CONSOLE_PATH);
            consoleFlow.addContent(httpListener);
        }
        else
        {
            Element httpInboundEndpoint = new Element("inbound-endpoint", HTTP_NAMESPACE.getNamespace());
            httpInboundEndpoint.setAttribute("address", API.DEFAULT_CONSOLE_PATH_INBOUND);
            consoleFlow.addContent(httpInboundEndpoint);
        }

        Element restProcessor = new Element("console", APIKitTools.API_KIT_NAMESPACE.getNamespace());
        if(!StringUtils.isEmpty(configRef)) {
            restProcessor.setAttribute("config-ref", configRef);
        }
        consoleFlow.addContent(restProcessor);

        mule.addContent(consoleFlow);

    }

    @Override
    public Element generate() {
        return consoleFlow;
    }
}
