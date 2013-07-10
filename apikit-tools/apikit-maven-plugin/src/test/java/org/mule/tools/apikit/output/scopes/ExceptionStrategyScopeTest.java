package org.mule.tools.apikit.output.scopes;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;
import org.mule.tools.apikit.Helper;

import static org.junit.Assert.assertTrue;

public class ExceptionStrategyScopeTest {
    @Test
    public void testGenerate() throws Exception {
        Document document = new Document();
        Element mule = new MuleScope(document).generate();
        new ExceptionStrategyScope(mule).generate();

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
                "<apikit:mapping-exception-strategy name=\"Global_Mapping_Exception_Strategy\">" +
                "<apikit:mapping statusCode=\"404\">" +
                "<apikit:exception value=\"apikit2.exception.NotFoundException\"/>" +
                "<set-payload value=\"resource not found\"/>" +
                "</apikit:mapping>" +
                "<apikit:mapping statusCode=\"405\">" +
                "<apikit:exception value=\"apikit2.exception.MethodNotAllowedException\"/>" +
                "<set-payload value=\"method not allowed\"/>" +
                "</apikit:mapping>" +
                "<apikit:mapping statusCode=\"415\">" +
                "<apikit:exception value=\"apikit2.exception.UnsupportedMediaTypeException\"/>" +
                "<set-payload value=\"unsupported media type\"/>" +
                "</apikit:mapping>" +
                "<apikit:mapping statusCode=\"406\">" +
                "<apikit:exception value=\"apikit2.exception.NotAcceptableException\"/>" +
                "<set-payload value=\"not acceptable\"/>" +
                "</apikit:mapping>" +
                "<apikit:mapping statusCode=\"400\">" +
                "<apikit:exception value=\"apikit2.exception.BadRequestException\"/>" +
                "<set-payload value=\"bad request\"/>" +
                "</apikit:mapping>" +
                "</apikit:mapping-exception-strategy>" +
                "</mule>";

        Diff diff = XMLUnit.compareXML(control, s);

        assertTrue(diff.toString(), diff.similar());
    }
}
