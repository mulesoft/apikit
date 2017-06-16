/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import static org.junit.Assert.assertEquals;
import static org.mule.raml.interfaces.model.IActionType.GET;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class APIKitFlowTest {

    private static final String RESOURCE = "/leagues/{leagueId}";
    private static final String BAD_RESOURCE = "leagues/{leagueId}";
    private static final String CONFIG_REF = "config-ref";
    private static final String MIME_TYPE = "text/xml";
    private static final String INVALID_ACTION ="INV";
    private static final String SEPARATOR = ":";

    @Test
    public void testAPIKitFlowName() {
        APIKitFlow flow = APIKitFlow.buildFromName(buildName(GET.toString(), RESOURCE, null, CONFIG_REF), Arrays.asList(new String[] {CONFIG_REF}));
        assertEquals(GET.toString().toLowerCase(), flow.getAction());
        assertEquals(RESOURCE, flow.getResource());
        assertEquals(CONFIG_REF, flow.getConfigRef());
    }

    @Test
    public void testAPIKitFlowNameWithContentType() {
        APIKitFlow flow = APIKitFlow.buildFromName(buildName(GET.toString(), RESOURCE, MIME_TYPE, CONFIG_REF), Arrays.asList(new String[] {CONFIG_REF}));
        assertEquals(GET.toString().toLowerCase(), flow.getAction());
        assertEquals(RESOURCE, flow.getResource());
        assertEquals(MIME_TYPE, flow.getMimeType());
        assertEquals(CONFIG_REF, flow.getConfigRef());
    }

    @Test
    public void testAPIKitFlowNameWithContentTypeNoConfigRef() {
        APIKitFlow flow = APIKitFlow.buildFromName(buildName(GET.toString(), RESOURCE, MIME_TYPE, null), Arrays.asList(new String[] {CONFIG_REF}));
        assertEquals(GET.toString().toLowerCase(), flow.getAction());
        assertEquals(RESOURCE, flow.getResource());
        assertEquals(MIME_TYPE, flow.getMimeType());
        assertEquals(APIKitFlow.UNNAMED_CONFIG_NAME, flow.getConfigRef());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAPIKitFlowNameInvalidAction() {
        APIKitFlow.buildFromName(buildName(INVALID_ACTION, RESOURCE, null, CONFIG_REF), Arrays.asList( new String[] {CONFIG_REF}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void APIKitFlowNameInvalidFormat() {
        APIKitFlow.buildFromName(buildName(GET.toString(), null, null, null), Arrays.asList( new String[] {CONFIG_REF}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void APIKitFlowNameInvalidResource() {
        APIKitFlow.buildFromName(buildName(GET.toString(), BAD_RESOURCE, null, CONFIG_REF), Arrays.asList( new String[] {CONFIG_REF}));
    }

    @Test
    public void APIKitFlowNameNoConfigRef() {
        APIKitFlow flow = APIKitFlow.buildFromName(buildName(GET.toString(), RESOURCE, null, null), Arrays.asList( new String[] {CONFIG_REF}));
        assertEquals(GET.toString().toLowerCase(), flow.getAction());
        assertEquals(RESOURCE, flow.getResource());
        assertEquals(APIKitFlow.UNNAMED_CONFIG_NAME, flow.getConfigRef());
    }

    private String buildName(String action, String resource, String mimeType, String config) {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(action.toLowerCase());
        if(!StringUtils.isEmpty(resource)) {
            nameBuilder.append(SEPARATOR).append(resource);
        }
        if(!StringUtils.isEmpty(mimeType)) {
            nameBuilder.append(SEPARATOR).append(mimeType);
        }
        if(!StringUtils.isEmpty(config)) {
            nameBuilder.append(SEPARATOR).append(config);
        }
        return nameBuilder.toString();
    }

}
