/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import static org.junit.Assert.assertTrue;

import org.mule.tools.apikit.Helper;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

public class ExceptionStrategyScopeTest {

    private static final String API_ID = "id";

    @Test
    public void testGenerate() throws Exception {
        Document document = new Document();
        Element mule = new MuleScope().generate();
        document.setRootElement(mule);
        mule.addContent(new ExceptionStrategyScope(API_ID).generate());

        String s = Helper.nonSpaceOutput(mule);

        String control = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\" \n" +
                "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "      xmlns:http=\"http://www.mulesoft.org/schema/mule/http\" \n" +
                "      xmlns:apikit=\"http://www.mulesoft.org/schema/mule/apikit\" \n" +
                "      xmlns:spring=\"http://www.springframework.org/schema/beans\" \n" +
                "      xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd\n" +
                "        http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd\n" +
                "        http://www.mulesoft.org/schema/mule/apikit http://www.mulesoft.org/schema/mule/apikit/current/mule-apikit.xsd\n" +
                "        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd\">" +
                "<apikit:mapping-exception-strategy name=\"id-apiKitGlobalExceptionMapping\">" +
                "<apikit:mapping statusCode=\"404\">" +
                "<apikit:exception value=\"org.mule.module.apikit.exception.NotFoundException\"/>" +
                "<set-property propertyName=\"Content-Type\" value=\"application/json\"/>" +
                "<set-payload value=\"{ &quot;message&quot;: &quot;Resource not found&quot; }\"/>" +
                "</apikit:mapping>" +
                "<apikit:mapping statusCode=\"405\">" +
                "<apikit:exception value=\"org.mule.module.apikit.exception.MethodNotAllowedException\"/>" +
                "<set-property propertyName=\"Content-Type\" value=\"application/json\"/>" +
                "<set-payload value=\"{ &quot;message&quot;: &quot;Method not allowed&quot; }\"/>" +
                "</apikit:mapping>" +
                "<apikit:mapping statusCode=\"415\">" +
                "<apikit:exception value=\"org.mule.module.apikit.exception.UnsupportedMediaTypeException\"/>" +
                "<set-property propertyName=\"Content-Type\" value=\"application/json\"/>" +
                "<set-payload value=\"{ &quot;message&quot;: &quot;Unsupported media type&quot; }\"/>" +
                "</apikit:mapping>" +
                "<apikit:mapping statusCode=\"406\">" +
                "<apikit:exception value=\"org.mule.module.apikit.exception.NotAcceptableException\"/>" +
                "<set-property propertyName=\"Content-Type\" value=\"application/json\"/>" +
                "<set-payload value=\"{ &quot;message&quot;: &quot;Not acceptable&quot; }\"/>" +
                "</apikit:mapping>" +
                "<apikit:mapping statusCode=\"400\">" +
                "<apikit:exception value=\"org.mule.module.apikit.exception.BadRequestException\"/>" +
                "<set-property propertyName=\"Content-Type\" value=\"application/json\"/>" +
                "<set-payload value=\"{ &quot;message&quot;: &quot;Bad request&quot; }\"/>" +
                "</apikit:mapping>" +
                "</apikit:mapping-exception-strategy>" +
                "</mule>";

        Diff diff = XMLUnit.compareXML(control, s);

        assertTrue(diff.toString(), diff.similar());
    }
}
