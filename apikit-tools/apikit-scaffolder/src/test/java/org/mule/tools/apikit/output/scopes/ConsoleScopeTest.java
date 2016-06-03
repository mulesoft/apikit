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
import org.mule.tools.apikit.model.HttpListenerConfig;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsoleScopeTest {

    @Test
    public void testGenerateConsoleFlow() throws Exception {
        Document document = new Document();
        Element mule =new MuleScope().generate();
        document.setRootElement(mule);
        APIKitConfig config = new APIKitConfig.Builder("path/to/file.raml").setExtensionEnabled(true).build();
        new APIKitConfigScope(config, mule, "3.7.3").generate();
        API api = mock(API.class);
        HttpListenerConfig listenerConfig = new HttpListenerConfig.Builder("HTTP_Listener_Configuration","localhost","7777","").build();

        when(api.getId()).thenReturn("file");
        when(api.getPath()).thenReturn("/api/*");
        when(api.getConfig()).thenReturn(config);
        when(api.getHttpListenerConfig()).thenReturn(listenerConfig);
        new HttpListenerConfigScope(api,mule).generate();
        new FlowScope(mule, "ExceptionStrategyNameHere", api, null, "HTTP_Listener_Configuration").generate();
        new ConsoleFlowScope(
                mule,
                api,
                api.getConfig().getName(),
                api.getHttpListenerConfig().getName()).generate();

        String s = Helper.nonSpaceOutput(mule);

        String control = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\" \n" +
                "      xmlns:apikit=\"http://www.mulesoft.org/schema/mule/apikit\" \n" +
                "      xmlns:http=\"http://www.mulesoft.org/schema/mule/http\" \n" +
                "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "      xmlns:spring=\"http://www.springframework.org/schema/beans\" \n" +
                "      xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd\n" +
                "        http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd\n" +
                "        http://www.mulesoft.org/schema/mule/apikit http://www.mulesoft.org/schema/mule/apikit/current/mule-apikit.xsd\n" +
                "        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd\">" +
                "<http:listener-config name=\"HTTP_Listener_Configuration\" host=\"localhost\" port=\"7777\"/>" +
                "<apikit:config raml=\"path/to/file.raml\" extensionEnabled=\"true\" consoleEnabled=\"false\" />" +
                "<flow name=\"file-main\">" +
                "<http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\"/>" +
                "<apikit:router />" +
                "<exception-strategy ref=\"ExceptionStrategyNameHere\"/>" +
                "</flow>" +
                "<flow name=\"file-console\">" +
                "<http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/console/*\" />" +
                "<apikit:console />" +
                "</flow>" +
                "</mule>";

        Diff diff = XMLUnit.compareXML(control, s);

        assertTrue(diff.toString(), diff.similar());
    }
}