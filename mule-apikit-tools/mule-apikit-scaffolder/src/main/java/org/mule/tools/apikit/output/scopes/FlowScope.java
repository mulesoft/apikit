/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

public class FlowScope implements Scope {


    private final Element main;

    public FlowScope(Element mule, String exceptionStrategyRef, API api, String configRef, String httpListenerConfigRef) {
        this(mule, exceptionStrategyRef, api, configRef, httpListenerConfigRef, true);
    }

    public FlowScope(Element mule, String exceptionStrategyRef, API api, String configRef, String httpListenerConfigRef, boolean isMuleEE) {

        main = new Element("flow", XMLNS_NAMESPACE.getNamespace());
        main.setAttribute("name", api.getId() + "-" + "main");

        MainFlowsUtils.generateListenerSource(httpListenerConfigRef, api.getPath(), main);

        Element restProcessor = new Element("router", APIKitTools.API_KIT_NAMESPACE.getNamespace());
        if(!StringUtils.isEmpty(configRef)) {
            restProcessor.setAttribute("config-ref", configRef);
        }

        Element errorHandler = ErrorHandlerScope.createForMainFlow(isMuleEE).generate();

        main.addContent(restProcessor);
        main.addContent(errorHandler);

        mule.addContent(main);
    }

    @Override
    public Element generate() {
        return main;
    }
}
