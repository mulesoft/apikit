package org.mule.tooling.apikit.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
		final String testName = "referenceFlowSimpleTestAddingNewGlobalElement";
		final String testFolder = "resources/" + testName + "/" + testName;
		final String testFolderMuleVersion = testFolder + nameAddition;
		final String defaultFileMuleVersion = "resources/default/default" + nameAddition;
		final String yamlFileInput = testFolder + "-input.yaml";
		final String xmlFilePart1 = testFolderMuleVersion + "-part1.xml";
	  	final String xmlFileExpected = testFolderMuleVersion +"-part2-expected.xml";
		final String projectName = "rfstange"+ System.currentTimeMillis();
		final String flowName = "simpleyamlfilerfstange"+nameAddition;
	  	final String yamlFilePath = "src/main/api";
	  	final String yamlFileName = flowName + ".yaml";
	  	XmlComparer comparer = new XmlComparer();
	  	final MuleStudioBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description",muleVersion);
	  	projectBot.openDefaultMflow(projectName);
	  	
	  	MuleGefEditor editordef = new MuleGefEditor(bot, projectName);
	  	editordef.changeTab(UILabels.TAB_3);
	  	
	  	String defaultExpected = comparer.readResource(defaultFileMuleVersion + "-expected.xml");
	  	String defaultActual = editordef.getTextOfTheTab();
	  	comparer.assertIdenticalXML("The default flow was not generated as expected. ", defaultExpected, defaultActual, true);
	  	String defaultModified = comparer.readResource(defaultFileMuleVersion + "-modified.xml");
	  	
	  	editordef.setTextOfTheTab(defaultModified);
	  	editordef.save();
	  	editordef.changeTab(UILabels.TAB_1);
	  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
	  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
	  	
	  	assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
	  	
	  	projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);
	  	
	  	MuleGefEditor editor = new MuleGefEditor(bot, flowName);
	  	editor.changeTab(UILabels.TAB_3);
	  	String modified = comparer.readResource(xmlFilePart1);

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
	    assertEquals("/resource1",globalElementWizard.getTableText(0, 0));
	    assertEquals("Get",globalElementWizard.getTableText(0, 1));
	    assertEquals("modifiedFlow",globalElementWizard.getTableText(0, 2));
	    globalElementWizard.clickOK();
	    
	    propertiesEditorBot.apply();
	    editor.changeTab(UILabels.TAB_3);
	    String modifiedExpected = comparer.readResource(xmlFileExpected);

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
		final String testName = "referenceFlowSimpleTestEditingGlobalElement";
		final String testFolder = "resources/" + testName + "/" + testName;
		final String testFolderMuleVersion = testFolder + nameAddition;
		
		final String yamlFileInput = testFolder + "-input.yaml";
		final String xmlFilePart1 = testFolderMuleVersion + "-part1.xml";
	  	final String xmlFileExpected = testFolderMuleVersion +"-part2-expected.xml";
	  	final String defaultFileMuleVersion = "resources/default/default" + nameAddition;
		final String projectName = "rfstege"+ System.currentTimeMillis();
		final String flowName = "simpleyamlfilerfstege" + nameAddition;
	  	final String yamlFilePath = "src/main/api";
	  	final String yamlFileName = flowName + ".yaml";
	  	XmlComparer comparer = new XmlComparer();
	  	final MuleStudioBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description",muleVersion);
	  	projectBot.openDefaultMflow(projectName);
	  	
	  	MuleGefEditor editordef = new MuleGefEditor(bot, projectName);
	  	editordef.changeTab(UILabels.TAB_3);
	  	
	  	String defaultExpected = comparer.readResource(defaultFileMuleVersion + "-expected.xml");
	  	String defaultActual = editordef.getTextOfTheTab();
	  	comparer.assertIdenticalXML("The default flow was not generated as expected. ", defaultExpected, defaultActual, true);
	  	String defaultModified = comparer.readResource(defaultFileMuleVersion + "-modified.xml");
	  	
	  	editordef.setTextOfTheTab(defaultModified);
	  	editordef.save();
	  	editordef.changeTab(UILabels.TAB_1);
	  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
	  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
	  	
	  	assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
	  	
	  	projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);
	  	
	  	MuleGefEditor editor = new MuleGefEditor(bot, flowName);
	  	editor.changeTab(UILabels.TAB_3);
	  	String modified = comparer.readResource(xmlFilePart1);

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
	    assertEquals("/resource1",globalElementWizard.getTableText(0, 0));
	    assertEquals("Get",globalElementWizard.getTableText(0, 1));
	    assertEquals("modifiedFlow",globalElementWizard.getTableText(0, 2));
	    globalElementWizard.clickOK();
	    
	    propertiesEditorBot.apply();
	    editor.changeTab(UILabels.TAB_3);
	    String modifiedExpected = comparer.readResource(xmlFileExpected);

        comparer.assertIdenticalXML("XML files are different. ", modifiedExpected, editor.getTextOfTheTab(), true);
        
        muleStudioBot.saveAll();
	}
	
	@Test
	public void tryAddFlowMappingUsingInvalidYamlFileMule34() throws Exception{
		tryAddFlowMappingUsingInvalidYamlFile(UILabels.MULE_34, "Mule34");
	}

	@Test
	public void tryAddFlowMappingUsingInvalidYamlFileMule35() throws Exception{
		tryAddFlowMappingUsingInvalidYamlFile(UILabels.MULE_35, "Mule35");
	}
	
	public void tryAddFlowMappingUsingInvalidYamlFile(String muleVersion,String nameAddition) throws Exception{
		final String testName = "tryAddFlowMappingUsingInvalidYamlFile";
		final String testFolder = "resources/" + testName + "/" + testName;
		final String testFolderMuleVersion = testFolder + nameAddition;
		final String defaultFileMuleVersion = "resources/default/default" + nameAddition;
		final String yamlFileInput = testFolder + "-input.yaml";
	  	final String xmlFileExpected = testFolderMuleVersion +"-expected.xml";
		
		final String projectName = "tafmuiyf"+ System.currentTimeMillis();
		final String flowName = "simpleyamlfiletafmuiyf" + nameAddition;
	  	final String yamlFilePath = "src/main/api";
	  	final String yamlFileName = flowName + ".yaml";
	  	final XmlComparer comparer = new XmlComparer();
	  	
	  	final MuleStudioBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description",muleVersion);
	  	projectBot.openDefaultMflow(projectName);
	  	
	  	
	  	MuleGefEditor editordef = new MuleGefEditor(bot, projectName);
	  	editordef.changeTab(UILabels.TAB_3);
	  	
	  	String defaultExpected = comparer.readResource(defaultFileMuleVersion + "-expected.xml");
	  	String defaultActual = editordef.getTextOfTheTab();
	  	comparer.assertIdenticalXML("The default flow was not generated as expected. ", defaultExpected, defaultActual, true);
	  	String defaultModified = comparer.readResource(defaultFileMuleVersion + "-modified.xml");
	  	
	  	editordef.setTextOfTheTab(defaultModified);
	  	editordef.save();
	  	editordef.changeTab(UILabels.TAB_1);
	  	
	  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
	  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
	  	
	  	assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
	  	
	  	projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);
	  	
	  	MuleGefEditor editor = new MuleGefEditor(bot, flowName);
	  	editor.changeTab(UILabels.TAB_1);
	  	editor.clickOnAbox("APIkit Router");
	  	
	        
	    MulePropertiesEditorBot propertiesEditorBot = new MulePropertiesEditorBot(bot);
	    
	    propertiesEditorBot.clickTooltipButton("Edit");
	    
	    GlobalElementWizardEditorBot globalElementWizard = new GlobalElementWizardEditorBot(bot);
	    globalElementWizard.setYamlFileName("yamlFileThatNotExists.yaml");
	    globalElementWizard.clickOnAddAnewMapping();
	    
	    assertEquals("Could not add flow mapping", bot.activeShell().getText().toString());
	    bot.activeShell().bot().button("OK").click();
	    globalElementWizard.clickOK();
	    
	    propertiesEditorBot.apply();
	    editor.changeTab(UILabels.TAB_3);
	    String modifiedExpected = comparer.readResource(xmlFileExpected);
	   
        comparer.assertIdenticalXML("XML files are different. ", modifiedExpected, editor.getTextOfTheTab(), true);
        
        muleStudioBot.saveAll();
	}
	
	@Test
	public void tryDeleteFlowMappingUsingInvalidYamlFileMule34() throws Exception{
		tryDeleteFlowMappingUsingInvalidYamlFile(UILabels.MULE_34, "Mule34");
	}

	@Test
	public void tryDeleteFlowMappingUsingInvalidYamlFileMule35() throws Exception{
		tryDeleteFlowMappingUsingInvalidYamlFile(UILabels.MULE_35, "Mule35");
	}
	
	public void tryDeleteFlowMappingUsingInvalidYamlFile(String muleVersion,String nameAddition) throws Exception{
		final String testName = "tryAddFlowMappingUsingInvalidYamlFile";
		final String testFolder = "resources/" + testName + "/" + testName;
		final String testFolderMuleVersion = testFolder + nameAddition;
		
		final String yamlFileInput = testFolder + "-input.yaml";
	  	final String xmlFileExpected = testFolderMuleVersion +"-expected.xml";
	  	final String defaultFileMuleVersion = "resources/default/default" + nameAddition;
		final String projectName = "tdfmuiyf"+ System.currentTimeMillis();
		final String flowName = "simpleyamlfiletdfmuiyf" + nameAddition;
	  	final String yamlFilePath = "src/main/api";
	  	final String yamlFileName = flowName + ".yaml";
	  	XmlComparer comparer = new XmlComparer();
	  	final MuleStudioBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description",muleVersion);
	  	projectBot.openDefaultMflow(projectName);
	  	
	  	MuleGefEditor editordef = new MuleGefEditor(bot, projectName);
	  	editordef.changeTab(UILabels.TAB_3);
	  	
	  	String defaultExpected = comparer.readResource(defaultFileMuleVersion + "-expected.xml");
	  	String defaultActual = editordef.getTextOfTheTab();
	  	comparer.assertIdenticalXML("The default flow was not generated as expected. ", defaultExpected, defaultActual, true);
	  	String defaultModified = comparer.readResource(defaultFileMuleVersion + "-modified.xml");
	  	
	  	editordef.setTextOfTheTab(defaultModified);
	  	editordef.save();
	  	editordef.changeTab(UILabels.TAB_1);
	  	
	  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
	  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
	  	
	  	assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
	  	
	  	projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);
	  	
	  	MuleGefEditor editor = new MuleGefEditor(bot, flowName);
	  	editor.changeTab(UILabels.TAB_1);
	  	editor.clickOnAbox("APIkit Router");
	  	
	        
	    MulePropertiesEditorBot propertiesEditorBot = new MulePropertiesEditorBot(bot);
	    
	    propertiesEditorBot.clickTooltipButton("Edit");
	    
	    GlobalElementWizardEditorBot globalElementWizard = new GlobalElementWizardEditorBot(bot);
	    globalElementWizard.setYamlFileName("yamlFileThatNotExists.yaml");
	    globalElementWizard.clickOnRemoveCurrentMapping();
	
	    globalElementWizard.clickOK();
	    
	    propertiesEditorBot.apply();
	    editor.changeTab(UILabels.TAB_3);
	    String modifiedExpected = comparer.readResource(xmlFileExpected);
	    
        comparer.assertIdenticalXML("XML files are different. ", modifiedExpected, editor.getTextOfTheTab(), true);
        
        muleStudioBot.saveAll();
	}
    
	@Test
	public void referenceFlowLeaguesTestEditingGlobalElementMule34() throws Exception{
		referenceFlowLeaguesTestEditingGlobalElement(UILabels.MULE_34, "Mule34");
	}

	@Test
	public void referenceFlowLeaguesTestEditingGlobalElementMule35() throws Exception{
		referenceFlowLeaguesTestEditingGlobalElement(UILabels.MULE_35, "Mule35");
	}
	
	public void referenceFlowLeaguesTestEditingGlobalElement(String muleVersion,String nameAddition) throws Exception{
		final String testName = "referenceFlowLeaguesTestEditingGlobalElement";
		final String testFolder = "resources/" + testName + "/" + testName;
		final String testFolderMuleVersion = testFolder + nameAddition;
		
		final String yamlFileInput = testFolder + "-input.yaml";
	  	final String xmlFilePart1 = testFolderMuleVersion +"-part1-expected.xml";
		final String xmlFilePart2 = testFolderMuleVersion +"-part2-modified.xml";
	  	final String xmlFileExpected = testFolderMuleVersion +"-part3-expected.xml";
	  	final String defaultFileMuleVersion = "resources/default/default" + nameAddition;
		final String projectName = "rfltege"+ System.currentTimeMillis();
		final String flowName = "leaguesyamlfilerfltege" + nameAddition;
	  	final String yamlFilePath = "src/main/api";
	  	final String yamlFileName = flowName + ".yaml";
	  	final XmlComparer comparer = new XmlComparer();
	  	final MuleStudioBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description",muleVersion);
	  	projectBot.openDefaultMflow(projectName);
	  	
	  	MuleGefEditor editordef = new MuleGefEditor(bot, projectName);
	  	editordef.changeTab(UILabels.TAB_3);
	  	
	  	String defaultExpected = comparer.readResource(defaultFileMuleVersion + "-expected.xml");
	  	String defaultActual = editordef.getTextOfTheTab();
	  	comparer.assertIdenticalXML("The default flow was not generated as expected. ", defaultExpected, defaultActual, true);
	  	String defaultModified = comparer.readResource(defaultFileMuleVersion + "-modified.xml");
	  	
	  	editordef.setTextOfTheTab(defaultModified);
	  	editordef.save();
	  	editordef.changeTab(UILabels.TAB_1);
	  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
	  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
	  	
	  	assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
	  	
	  	projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);
	  	
	  	MuleGefEditor editor = new MuleGefEditor(bot, flowName);
	  	editor.changeTab(UILabels.TAB_3);
	  	String part1expected = comparer.readResource(xmlFilePart1);

	  	String part1actual = editor.getTextOfTheTab();
	  	comparer.assertIdenticalXML("The generated xml file is not the one that was expected", part1expected, part1actual , true);
	  	String part2 = comparer.readResource(xmlFilePart2);
	  	editor.setTextOfTheTab(part2);
	  	editor.save();
	  	
	  	editor.changeTab(UILabels.TAB_1);
	  	
	  	editor.clickOnAbox("APIkit Router");
	  	
	        
	    MulePropertiesEditorBot propertiesEditorBot = new MulePropertiesEditorBot(bot);
	    
	    propertiesEditorBot.clickTooltipButton("Edit");
	    
	    GlobalElementWizardEditorBot globalElementWizard = new GlobalElementWizardEditorBot(bot);
	    globalElementWizard.setYamlFileName(yamlFileName);
	    globalElementWizard.clickOnAddAnewMapping();
	    
	    FlowMappingEditor flowMappingEditor = new FlowMappingEditor(bot);
	    flowMappingEditor.setResource("/teams");
	    flowMappingEditor.setAction("Post");
	    flowMappingEditor.setFlow("postTeam");
	    flowMappingEditor.apply();
	    
	    assertEquals("/teams",globalElementWizard.getTableText(0, 0));
	    assertEquals("Post",globalElementWizard.getTableText(0, 1));
	    assertEquals("postTeam",globalElementWizard.getTableText(0, 2));
	    globalElementWizard.clickOK();
	    
	    propertiesEditorBot.apply();
	    editor.changeTab(UILabels.TAB_3);
	    String modifiedExpected = comparer.readResource(xmlFileExpected);

        comparer.assertIdenticalXML("XML files are different. ", modifiedExpected, editor.getTextOfTheTab(), true);
        
        muleStudioBot.saveAll();
	}
	
	@Test
	public void referenceInexistentFlowLeaguesTestEditingGlobalElementMule34() throws Exception{
		referenceInexistentFlowLeaguesTestEditingGlobalElement(UILabels.MULE_34, "Mule34");
	}

	@Test
	public void referenceInexistentFlowLeaguesTestEditingGlobalElementMule35() throws Exception{
		referenceInexistentFlowLeaguesTestEditingGlobalElement(UILabels.MULE_35, "Mule35");
	}
	
	public void referenceInexistentFlowLeaguesTestEditingGlobalElement(String muleVersion,String nameAddition) throws Exception{
		final String testName = "referenceInexistentFlowLeaguesTestEditingGlobalElement";
		final String testFolder = "resources/" + testName + "/" + testName;
		final String testFolderMuleVersion = testFolder + nameAddition;
		
		final String yamlFileInput = testFolder + "-input.yaml";
	  	final String xmlFilePart1 = testFolderMuleVersion +"-part1-expected.xml";	
	  	final String xmlFileExpected = testFolderMuleVersion +"-part2-expected.xml";
	  	final String defaultFileMuleVersion = "resources/default/default" + nameAddition;
		
		final String projectName = "rifltege"+ System.currentTimeMillis();
		final String flowName = "leaguesyamlfilerifltege" + nameAddition;
	  	final String yamlFilePath = "src/main/api";
	  	final String yamlFileName = flowName + ".yaml";
	  	final XmlComparer comparer = new XmlComparer();
	  	final MuleStudioBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description",muleVersion);
	  	projectBot.openDefaultMflow(projectName);
	  	
	  	MuleGefEditor editordef = new MuleGefEditor(bot, projectName);
	  	editordef.changeTab(UILabels.TAB_3);
	  	
	  	String defaultExpected = comparer.readResource(defaultFileMuleVersion + "-expected.xml");
	  	String defaultActual = editordef.getTextOfTheTab();
	  	comparer.assertIdenticalXML("The default flow was not generated as expected. ", defaultExpected, defaultActual, true);
	  	String defaultModified = comparer.readResource(defaultFileMuleVersion + "-modified.xml");
	  	
	  	editordef.setTextOfTheTab(defaultModified);
	  	editordef.save();
	  	editordef.changeTab(UILabels.TAB_1);
	  	
	  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
	  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
	  	
	  	assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
	  	
	  	projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);
	  	
	  	MuleGefEditor editor = new MuleGefEditor(bot, flowName);
	  	editor.changeTab(UILabels.TAB_3);
	  	String part1expected = comparer.readResource(xmlFilePart1);

	  	String part1actual = editor.getTextOfTheTab();
	  	comparer.assertIdenticalXML("The generated xml file is not the one that was expected", part1expected, part1actual , true);
	  	
	  	editor.changeTab(UILabels.TAB_1);
	  	
	  	editor.clickOnAbox("APIkit Router");
	  	
	        
	    MulePropertiesEditorBot propertiesEditorBot = new MulePropertiesEditorBot(bot);
	    
	    propertiesEditorBot.clickTooltipButton("Edit");
	    
	    GlobalElementWizardEditorBot globalElementWizard = new GlobalElementWizardEditorBot(bot);
	    globalElementWizard.setYamlFileName(yamlFileName);
	    globalElementWizard.clickOnAddAnewMapping();
	    
	    FlowMappingEditor flowMappingEditor = new FlowMappingEditor(bot);
	    flowMappingEditor.setResource("/teams");
	    flowMappingEditor.setAction("Post");
	    flowMappingEditor.setNewFlow("inexistentFlow");
	    flowMappingEditor.apply();
	    assertEquals("/teams",globalElementWizard.getTableText(0, 0));
	    assertEquals("Post",globalElementWizard.getTableText(0, 1));
	    assertEquals("inexistentFlow",globalElementWizard.getTableText(0, 2));
	    globalElementWizard.clickOK();
	    
	    propertiesEditorBot.apply();
	    editor.changeTab(UILabels.TAB_3);
	    String modifiedExpected = comparer.readResource(xmlFileExpected);

        comparer.assertIdenticalXML("XML files are different. ", modifiedExpected, editor.getTextOfTheTab(), true);
        
        muleStudioBot.saveAll();
	}
    
    @AfterClass
    public static void afterClass() {
    	
    	muleStudioBot.saveAll();
    	muleStudioBot.closeAll();
    }
}
