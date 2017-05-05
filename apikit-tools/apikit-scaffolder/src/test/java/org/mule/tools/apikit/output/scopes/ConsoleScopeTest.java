/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.apikit.output.scopes;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;
import org.mule.tools.apikit.Helper;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.HttpListener4xConfig;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsoleScopeTest {

    @Test
    public void testGenerateConsoleFlow() throws Exception {
        Document document = new Document();
        Element mule =new MuleScope(false).generate();
        document.setRootElement(mule);
        APIKitConfig config = new APIKitConfig();
        config.setRaml("path/to/file.raml");
        config.setExtensionEnabled(true);
        new APIKitConfigScope(config, mule).generate();
        API api = mock(API.class);
        HttpListener4xConfig listenerConfig = new HttpListener4xConfig("HTTP_Listener_Configuration", "localhost", "7777", "HTTP", "");

        when(api.getId()).thenReturn("file");
        when(api.getPath()).thenReturn("/api/*");
        when(api.getConfig()).thenReturn(config);
        when(api.getHttpListenerConfig()).thenReturn(listenerConfig);
        new HttpListenerConfigMule4Scope(api, mule).generate();
        new FlowScope(mule, "ExceptionStrategyNameHere", api, null, "HTTP_Listener_Configuration").generate();
        new ConsoleFlowScope(
                mule,
                api,
                api.getConfig().getName(),
                api.getHttpListenerConfig().getName()).generate();

        String s = Helper.nonSpaceOutput(mule);

        String control = "<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\" xmlns:apikit=\"http://www.mulesoft.org/schema/mule/apikit\" xmlns:http=\"http://www.mulesoft.org/schema/mule/http\" xmlns:spring=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd http://www.mulesoft.org/schema/mule/apikit http://www.mulesoft.org/schema/mule/apikit/current/mule-apikit.xsd http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd \"><apikit:config raml=\"path/to/file.raml\" extensionEnabled=\"true\" outboundHeadersMapName=\"outboundHeaders\" httpStatusVarName=\"httpStatus\" /><http:listener-config name=\"HTTP_Listener_Configuration\"><http:listener-connection host=\"localhost\" port=\"7777\" /></http:listener-config><flow name=\"file-main\"><http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\" interpretRequestErrors=\"true\"><http:response statusCode=\"#[variables.httpStatus default 200]\"><http:headers>#[variables.outboundHeaders]</http:headers></http:response></http:listener><apikit:router /></flow><flow name=\"file-console\"><http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/console/*\" interpretRequestErrors=\"true\"><http:response statusCode=\"#[variables.httpStatus default 200]\"><http:headers>#[variables.outboundHeaders]</http:headers></http:response></http:listener><apikit:console /></flow></mule>";

        Diff diff = XMLUnit.compareXML(control, s);

        assertTrue(diff.toString(), diff.similar());
    }
}