/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import static org.mule.tools.apikit.output.MuleConfigGenerator.HTTP_NAMESPACE;

import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.HttpListenerConfig;

import org.jdom2.Element;

public class HttpListenerConfigScope implements Scope
{
    private final Element mule;
    private final Element httpListenerConfig;

    public HttpListenerConfigScope(API api, Element mule)
    {
        this.mule = mule;

        if (api.getHttpListenerConfig() != null)
        {
            httpListenerConfig = new Element(HttpListenerConfig.ELEMENT_NAME, HTTP_NAMESPACE.getNamespace());
            httpListenerConfig.setAttribute("name", api.getHttpListenerConfig().getName());
            httpListenerConfig.setAttribute("host", api.getHttpListenerConfig().getHost());
            httpListenerConfig.setAttribute("port", api.getHttpListenerConfig().getPort());
            String basePath = api.getHttpListenerConfig().getBasePath();
            if (basePath != null && basePath != "/" && basePath != "")
            {
                httpListenerConfig.setAttribute("basePath", api.getHttpListenerConfig().getBasePath());
            }
            mule.addContent(httpListenerConfig);
        }
        else
            httpListenerConfig = null;
    }

    @Override
    public Element generate()
    {
        return httpListenerConfig;

    }
}