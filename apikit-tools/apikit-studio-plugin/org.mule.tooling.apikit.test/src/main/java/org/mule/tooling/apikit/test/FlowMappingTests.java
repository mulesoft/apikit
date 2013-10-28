package org.mule.tooling.apikit.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mule.tooling.apikit.test.api.APIDefinitionEditor;
import org.mule.tooling.apikit.test.api.FlowMappingEditor;
import org.mule.tooling.apikit.test.api.GlobalElementWizardEditorBot;
import org.mule.tooling.apikit.test.api.MuleGefEditor;
import org.mule.tooling.apikit.test.api.MulePropertiesEditorBot;
import org.mule.tooling.apikit.test.api.MuleStudioBot;
import org.mule.tooling.apikit.test.api.UILabels;
import org.mule.tooling.apikit.test.api.XmlComparer;

@RunWith(SWTBotJunit4ClassRunner.class)
public class FlowMappingTests {
   
	private static SWTWorkbenchBot bot;
    private static MuleStudioBot muleStudioBot;
    
   @BeforeClass
    public static void beforeClass() throws Exception {
        // increase timeout to 8 seconds
        SWTBotPreferences.TIMEOUT = 8000;
        // increase timeout to 1 second
        SWTBotPreferences.PLAYBACK_DELAY = 10;
        bot = new SWTWorkbenchBot();
        muleStudioBot = new MuleStudioBot(bot);
    }
   
   @Test
   public void referenceFlowSimpleTestAddingNewGlobalElementMule34() throws Exception{
	   referenceFlowSimpleTestAddingNewGlobalElement(UILabels.MULE_34, "Mule34");
   }
   
   @Test
   public void referenceFlowSimpleTestAddingNewGlobalElementMule35() throws Exception{
	   referenceFlowSimpleTestAddingNewGlobalElement(UILabels.MULE_35, "Mule35");
   }
	
	public void referenceFlowSimpleTestAddingNewGlobalElement(String muleVersion,String nameAddition) throws Exception{
		final String yamlFileInput = "resources/referenceFlowSimpleTestAddingNewGlobalElement-input.yaml";
		final String xmlFilePart1 = "resources/referenceFlowSimpleTestAddingNewGlobalElement" + nameAddition+ "-part1.xml";
	  	final String xmlFileExpected = "resources/referenceFlowSimpleTestAddingNewGlobalElement"+ nameAddition+"-part2-expected.xml";
		final String projectName = "rfstange"+ System.currentTimeMillis();
		final String flowName = "simpleyamlfilerfstange"+nameAddition;
	  	final String yamlFilePath = "src/main/api";
	  	final String yamlFileName = flowName + ".yaml";
	  	
	  	final MuleStudioBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description",muleVersion);
	  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
	  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
	  	
	  	assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
	  	
	  	projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);
	  	
	  	MuleGefEditor editor = new MuleGefEditor(bot, flowName);
	  	editor.changeTab(UILabels.TAB_3);
	  	String modified = readResource(xmlFilePart1);

	  	editor.setTextOfTheTab(modified);
	  	editor.save();
	  	editor.changeTab(UILabels.TAB_1);
	  	
	  	editor.clickOnAbox("APIkit Router");
	  	
	        
	    MulePropertiesEditorBot propertiesEditorBot = new MulePropertiesEditorBot(bot);
	    
	    propertiesEditorBot.clickTooltipButton("Add");
	    
	    GlobalElementWizardEditorBot globalElementWizard = new GlobalElementWizardEditorBot(bot);
	    globalElementWizard.setYamlFileName(yamlFileName);
	    globalElementWizard.clickOnAddAnewMapping();
	    
	    FlowMappingEditor flowMappingEditor = new FlowMappingEditor(bot);
	    flowMappingEditor.setResource("/resource1");
	    flowMappingEditor.setAction("Get");
	    flowMappingEditor.setFlow("modifiedFlow");
	    flowMappingEditor.apply();
	    
	    globalElementWizard.clickOK();
	    
	    propertiesEditorBot.apply();
	    editor.changeTab(UILabels.TAB_3);
	    String modifiedExpected = readResource(xmlFileExpected);

	    
	  	XmlComparer comparer = new XmlComparer(bot);
        comparer.assertIdenticalXML("XML files are different. ", modifiedExpected, editor.getTextOfTheTab(), true);
        
        muleStudioBot.saveAll();
	}
	
	@Test
	public void referenceFlowSimpleTestEditingGlobalElementMule34() throws Exception{
		referenceFlowSimpleTestEditingGlobalElement(UILabels.MULE_34, "Mule34");
	}

	@Test
	public void referenceFlowSimpleTestEditingGlobalElementMule35() throws Exception{
		referenceFlowSimpleTestEditingGlobalElement(UILabels.MULE_35, "Mule35");
	}
	
	public void referenceFlowSimpleTestEditingGlobalElement(String muleVersion,String nameAddition) throws Exception{
		final String yamlFileInput = "resources/referenceFlowSimpleTestEditingGlobalElement-input.yaml";
		final String xmlFilePart1 = "resources/referenceFlowSimpleTestEditingGlobalElement"+ nameAddition +"-part1.xml";
	  	final String xmlFileExpected = "resources/referenceFlowSimpleTestEditingGlobalElement"+ nameAddition +"-part2-expected.xml";
		final String projectName = "rfstege"+ System.currentTimeMillis();
		final String flowName = "simpleyamlfilerfstege" + nameAddition;
	  	final String yamlFilePath = "src/main/api";
	  	final String yamlFileName = flowName + ".yaml";
	  	
	  	final MuleStudioBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description",muleVersion);
	  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
	  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
	  	
	  	assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
	  	
	  	projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);
	  	
	  	MuleGefEditor editor = new MuleGefEditor(bot, flowName);
	  	editor.changeTab(UILabels.TAB_3);
	  	String modified = readResource(xmlFilePart1);

	  	editor.setTextOfTheTab(modified);
	  	editor.save();
	  	editor.changeTab(UILabels.TAB_1);
	  	
	  	editor.clickOnAbox("APIkit Router");
	  	
	        
	    MulePropertiesEditorBot propertiesEditorBot = new MulePropertiesEditorBot(bot);
	    
	    propertiesEditorBot.clickTooltipButton("Edit");
	    
	    GlobalElementWizardEditorBot globalElementWizard = new GlobalElementWizardEditorBot(bot);
	    globalElementWizard.setYamlFileName(yamlFileName);
	    globalElementWizard.clickOnAddAnewMapping();
	    
	    FlowMappingEditor flowMappingEditor = new FlowMappingEditor(bot);
	    flowMappingEditor.setResource("/resource1");
	    flowMappingEditor.setAction("Get");
	    flowMappingEditor.setFlow("modifiedFlow");
	    flowMappingEditor.apply();
	    
	    globalElementWizard.clickOK();
	    
	    propertiesEditorBot.apply();
	    editor.changeTab(UILabels.TAB_3);
	    String modifiedExpected = readResource(xmlFileExpected);

	    
	  	XmlComparer comparer = new XmlComparer(bot);
        comparer.assertIdenticalXML("XML files are different. ", modifiedExpected, editor.getTextOfTheTab(), true);
        
        muleStudioBot.saveAll();
	}
	
    protected String readResource(String configName) throws IOException {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(configName);
        return IOUtils.toString(resourceAsStream);
    }
    
    @AfterClass
    public static void afterClass() {
    	
    	muleStudioBot.saveAll();
    	muleStudioBot.closeAll();
    }
}
