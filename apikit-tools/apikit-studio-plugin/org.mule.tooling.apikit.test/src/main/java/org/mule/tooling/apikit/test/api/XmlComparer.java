package org.mule.tooling.apikit.test.api;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.apache.commons.io.IOUtils;

public class XmlComparer {
		
	SWTWorkbenchBot bot;
	public XmlComparer (SWTWorkbenchBot bot){
		this.bot = bot;
	}

    public void assertIdenticalXML(String errorMessage, String expectedStream, String actualStream, boolean ignoreWhitespace) throws Exception {
        XMLUnit.setIgnoreWhitespace(ignoreWhitespace);
        Diff diff = new Diff(expectedStream, actualStream);
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
        DetailedDiff detailedDiff = new DetailedDiff(diff);
        assertTrue(errorMessage + " " + detailedDiff.toString(), detailedDiff.identical());
    }
    
    protected String readResource(String configName) throws IOException {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(configName);
        return IOUtils.toString(resourceAsStream);
    }
    
    public void compareToTheXMLUsingUI(String errorMessage, String flowName, String expectedFilePath, boolean ignoreWhitespace) throws Exception{
    	bot.editorByTitle(flowName).bot().cTabItem("Configuration XML").activate();
	  	String actualXmlStream = bot.editorByTitle(flowName).toTextEditor().getText();
    	String expectedXmlStream = readResource(expectedFilePath);
		assertIdenticalXML(errorMessage,expectedXmlStream, actualXmlStream,true);
    }
    
}
