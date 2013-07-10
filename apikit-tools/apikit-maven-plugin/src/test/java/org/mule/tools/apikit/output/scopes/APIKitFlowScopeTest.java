package org.mule.tools.apikit.output.scopes;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;
import org.mule.tools.apikit.Helper;
import org.mule.tools.apikit.output.GenerationModel;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class APIKitFlowScopeTest {
    @Test
    public void testGenerate() throws Exception {
        GenerationModel flowEntry = mock(GenerationModel.class);
        when(flowEntry.getRelativeURI()).thenReturn("/pet");
        when(flowEntry.getVerb()).thenReturn("GET");
        when(flowEntry.getName()).thenReturn("retrievePet");
        when(flowEntry.getExample()).thenReturn("Hello world!");

        Document doc = new Document();
        Element mule = new Element("mule");

        new APIKitFlowScope(flowEntry, mule);
        doc.setContent(mule);

        String name = doc.getRootElement().getChildren().get(0).getAttribute("name").getValue();

        String s = Helper.nonSpaceOutput(doc);

        Diff diff = XMLUnit.compareXML(String.format("<apikit:flow " +
                "xmlns:apikit='http://www.mulesoft.org/schema/mule/apikit' " +
                "resource='/pet' action='GET' name='%s'><set-payload " +
                "xmlns='http://www.mulesoft.org/schema/mule/core' " +
                "value='Hello world!' /></apikit:flow>",
                name), s);

        assertTrue(diff.toString(), diff.similar());
    }
}
