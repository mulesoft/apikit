/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.mule.raml.interfaces.model.IActionType;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class APIKitFlow {

    public static final String UNNAMED_CONFIG_NAME = "noNameConfig";
    private final String action;
    private final String resource;
    private final String configRef;
    private final String mimeType;

    public static final String APIKIT_FLOW_NAME_FORMAT = "^([^:]+):(/[^:]*)(:([^:]+))?(:(.*))?$";

    public APIKitFlow(final String action, final String resource, final String mimeType, String configRef) {
        this.action = action;
        this.resource = resource;
        this.mimeType = mimeType;
        this.configRef = configRef != null ? configRef : UNNAMED_CONFIG_NAME;
    }

    public String getAction() {
        return action;
    }

    public String getResource() {
        return resource;
    }

    public String getMimeType() { return mimeType; }

    public String getConfigRef() {
        return configRef;
    }

    public static APIKitFlow  buildFromName(String name, Collection<String> existingConfigs) {
        if(StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Flow name cannot be null or empty");
        }

        Pattern flowNamePattern = Pattern.compile(APIKIT_FLOW_NAME_FORMAT);
        Matcher flowNameMatcher = flowNamePattern.matcher(name);

        if(!flowNameMatcher.find()) {
            throw new IllegalArgumentException("Invalid apikit flow name, expected format is: action:resource[:config]");
        }

        String action = flowNameMatcher.group(1);
        if(!isValidAction(action)) {
            throw new IllegalArgumentException(action + " is not a valid action type");
        }

        String resource = flowNameMatcher.group(2);

        String mimeType = null;
        String config = null;

        if(flowNameMatcher.groupCount() > 5) {
            if (flowNameMatcher.group(6) == null) {
                if (existingConfigs != null && existingConfigs.contains(flowNameMatcher.group(4))) {
                    config = flowNameMatcher.group(4);
                }
                else {
                    mimeType = flowNameMatcher.group(4);
                }
            }
            else {
                mimeType = flowNameMatcher.group(4);
                config = flowNameMatcher.group(6);
            }
        }

        return new APIKitFlow(action, resource, mimeType, config);
    }

    private static boolean isValidAction(String name) {
        for(IActionType actionType : IActionType.values()) {
            if(actionType.toString().equals(name.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
