/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.APIKitConfig;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

public class APIKitConfigScope implements Scope {

    private final Element mule;
    private final APIKitConfig config;

    public APIKitConfigScope(APIKitConfig config, Element mule) {
        this.mule = mule;
        this.config = config;
    }

    @Override
    public Element generate() {
        Element config = null;
        if(this.config != null) {
            config = new Element(APIKitConfig.ELEMENT_NAME,
                                            APIKitTools.API_KIT_NAMESPACE.getNamespace());

            if(!StringUtils.isEmpty(this.config.getName())) {
                config.setAttribute(APIKitConfig.NAME_ATTRIBUTE, this.config.getName());
            }

            config.setAttribute(APIKitConfig.RAML_ATTRIBUTE, this.config.getRaml());
            config.setAttribute(APIKitConfig.CONSOLE_ENABLED_ATTRIBUTE, String.valueOf(this.config.isConsoleEnabled()));
            config.setAttribute(APIKitConfig.CONSOLE_PATH_ATTRIBUTE, this.config.getConsolePath());

            mule.addContent(config);
        }
        return config;
    }
}
