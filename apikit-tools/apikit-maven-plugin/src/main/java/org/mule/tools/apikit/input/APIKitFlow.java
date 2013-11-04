/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.raml.model.ActionType;

public class APIKitFlow {

    private final String action;
    private final String resource;
    private final String configRef;

    public static final String APIKIT_FLOW_NAME_FORMAT = "^([^:]+):(/[^:]+)(:(.*))?$";

    public APIKitFlow(final String action, final String resource) {
        this(action, resource, null);
    }

    public APIKitFlow(final String action, final String resource, String configRef) {
        this.action = action;
        this.resource = resource;
        this.configRef = configRef;
    }

    public String getAction() {
        return action;
    }

    public String getResource() {
        return resource;
    }

    public String getConfigRef() {
        return configRef;
    }

    public static APIKitFlow buildFromName(String name) {
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
        String config = null;

        if(flowNameMatcher.groupCount() > 3) {
            config = flowNameMatcher.group(4);
        }

        return new APIKitFlow(action, resource, config);
    }

    private static boolean isValidAction(String name) {
        for(ActionType actionType : ActionType.values()) {
            if(actionType.toString().toLowerCase().equals(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
