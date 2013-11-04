/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.raml.model.ActionType;

public class APIKitFlowTest {

    private static final String RESOURCE = "/leagues/{leagueId}";
    private static final String BAD_RESOURCE = "leagues/{leagueId}";
    private static final String CONFIG_REF = "config-ref";
    private static final String INVALID_ACTION ="INV";
    private static final String SEPARATOR = ":";

    @Test
    public void testAPIKitFlowName() {
        APIKitFlow flow = APIKitFlow.buildFromName(buildName(ActionType.GET.toString(), RESOURCE, CONFIG_REF));
        assertEquals(ActionType.GET.toString().toLowerCase(), flow.getAction());
        assertEquals(RESOURCE, flow.getResource());
        assertEquals(CONFIG_REF, flow.getConfigRef());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAPIKitFlowNameInvalidAction() {
        APIKitFlow.buildFromName(buildName(INVALID_ACTION, RESOURCE, CONFIG_REF));
    }

    @Test(expected = IllegalArgumentException.class)
    public void APIKitFlowNameInvalidFormat() {
        APIKitFlow.buildFromName(buildName(ActionType.GET.toString(), null, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void APIKitFlowNameInvalidResource() {
        APIKitFlow.buildFromName(buildName(ActionType.GET.toString(), BAD_RESOURCE, CONFIG_REF));
    }

    @Test
    public void APIKitFlowNameNoConfigRef() {
        APIKitFlow flow = APIKitFlow.buildFromName(buildName(ActionType.GET.toString(), RESOURCE, null));
        assertEquals(ActionType.GET.toString().toLowerCase(), flow.getAction());
        assertEquals(RESOURCE, flow.getResource());
        assertNull(flow.getConfigRef());
    }

    private String buildName(String action, String resource, String config) {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(action.toLowerCase());
        if(!StringUtils.isEmpty(resource)) {
            nameBuilder.append(SEPARATOR).append(resource);
        }
        if(!StringUtils.isEmpty(config)) {
            nameBuilder.append(SEPARATOR).append(config);
        }
        return nameBuilder.toString();
    }

}
