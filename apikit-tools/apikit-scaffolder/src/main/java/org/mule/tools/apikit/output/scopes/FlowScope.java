/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import static org.mule.tools.apikit.output.MuleConfigGenerator.HTTPN_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.HTTP_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIKitConfig;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;
import org.mozilla.javascript.tools.debugger.Main;

public class FlowScope implements Scope {


    private final Element main;

    public FlowScope(Element mule, String exceptionStrategyRef, API api, String configRef, String httpListenerConfigRef) {
        main = new Element("flow", XMLNS_NAMESPACE.getNamespace());

        main.setAttribute("name", api.getId() + "-" + "main");

        if (httpListenerConfigRef != null)
        {
            MainFlowsUtils.generateListenerSource(httpListenerConfigRef, api.getPath(), main);
        }
        else
        {
            MainFlowsUtils.generateInboundSource(api.getBaseUri(), main);
        }

        Element restProcessor = new Element("router", APIKitTools.API_KIT_NAMESPACE.getNamespace());
        if(!StringUtils.isEmpty(configRef)) {
            restProcessor.setAttribute("config-ref", configRef);
        }
        main.addContent(restProcessor);
        //TODO ADD EXCEPTION STRATEGY
        //if (APIKitTools.usesListenersMuleV3(api.getMuleVersion()) || APIKitTools.defaultIsInboundEndpoint(api.getMuleVersion()))
        //{
        //    Element exceptionStrategy = new Element("exception-strategy", XMLNS_NAMESPACE.getNamespace());
        //    exceptionStrategy.setAttribute("ref", exceptionStrategyRef);
        //    main.addContent(exceptionStrategy);
        //}

        mule.addContent(main);
    }

    @Override
    public Element generate() {
        return main;
    }
}
