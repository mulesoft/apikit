package org.mule.tooling.apikit.test;


import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mule.tooling.apikit.test.api.APIDefinitionEditor;
import org.mule.tooling.apikit.test.api.MuleProjectBot;
import org.mule.tooling.apikit.test.api.MuleStudioBot;
import org.mule.tooling.apikit.test.api.XmlComparer;

//@RunWith(Suite.class)
//@SuiteClasses( {})
@RunWith(SWTBotJunit4ClassRunner.class)
public class ScaffolderTests {

    private static SWTWorkbenchBot bot;
    private static MuleStudioBot muleStudioBot;
    
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        // increase timeout to 10 seconds
        SWTBotPreferences.TIMEOUT = 8000;
        // increase timeout to 1 second
        SWTBotPreferences.PLAYBACK_DELAY = 10;
        // Don't use SWTWorkbenchBot here which relies on Platform 3.x
        bot = new SWTWorkbenchBot();
        muleStudioBot = new MuleStudioBot(bot);
    }

	
	  @Test
	  public void createSimpleExampleUsingScaffolderCompareXML() throws Exception{
		  	final String yamlFileInput = "resources/new-file-input.yaml";
		  	final String xmlFileExpected = "/resources/apikit-simple-test-expected.xml";
		  	createSimpleFileUsingScaffolderCompareXML(yamlFileInput, xmlFileExpected);
	        muleStudioBot.saveAll();
	  }
	  
	  public void createSimpleFileUsingScaffolderCompareXML(String yamlFileInput, String xmlFileExpected) throws Exception{
		  final String projectName = "dnd"+ System.currentTimeMillis();
		  final String flowName = "yamlfile";
		  	final String yamlFilePath = "src/main/api";
		  	final String yamlFileName = "yamlfile.yaml";
		  	
		  	final MuleProjectBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description");
		  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
		  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
		  	/*if (yamlFileInput == "resources/leagues-input.yaml"){
		  		Thread.sleep(10000);
		  	}*/
		  	assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
		  	
		  	projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);

		  	XmlComparer comparer = new XmlComparer(bot);
	        comparer.compareToTheXMLUsingUI(flowName, xmlFileExpected,true);

	  }
	  
	  

	  @Ignore
	  public void createLeaguesExampleUsingScaffolder() throws Exception{
		  final String yamlFileInput = "resources/leagues-input.yaml";
		  final String xmlFileExpected = "/resources/apikit-leagues-example-expected.xml";
		  createSimpleFileUsingScaffolderCompareXML(yamlFileInput, xmlFileExpected);
		  muleStudioBot.saveAll();
	  }

	 /* @Test
	  public void createSalesExampleUsingScaffolderCompareXML() throws Exception{
		  final String yamlFileInput = "resources/sales-input.yaml";
		  final String xmlFileExpected = "/resources/apikit-sales-example-expected.xml";
		  createSimpleFileUsingScaffolderCompareXML(yamlFileInput, xmlFileExpected);
	  }*/
	  
	  /*
	  @Test
	  public void createSimpleExampleUsingScaffolderCompareUI() throws Exception{
		  	final String yamlFileInput = "resources/new-file-input.yaml";
		  	final String xmlFileExpected = "/resources/apikit-simple-test-expected.xml";
		  
		  	final String projectName = "dnd";
		  	final String yamlFilePath = "src/main/api";
		  	final String yamlFileName = "yamlfile.yaml";
		  	
		  	final MuleProjectBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description");
		  	//final MuleConfigurationEditorBot muleConfigurationBot = projectBot.openMuleConfiguration("test");
		  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
		  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
		  	
		  	try{
		  		projectBot.generateFlows(yamlFilePath, yamlFileName);
		  	}
		  	catch(Exception e){
		  		throw new Exception("Cannot generate flows due to invalid yaml file.");
		  	}
		  	
		  	
		  	
		  	
		  	
		  	//bot.cTabItem("yamlFile").activate();
		  	//muleConfigurationBot.getFlow("main");
		  	//MulePropertiesEditorBot httpEditorBot = muleConfigurationBot.editElementProperties("main","HTTP");
		  	
		  	SWTBotGefEditor editor = new SWTBotGefEditor(bot.activeEditor().getReference(), bot);
		  	editor.click("HTTP");
		  	
		  	String httpName = bot.activeEditor().bot().textWithLabel("Display Name:").getText();
		  	assertEquals("HTTP", httpName);
		  	//MuleConfigurationEditorBot routerEditorBot = muleConfigurationBot.getGlobalElement("http://www.mulesoft.org/schema/mule/apikit/config","Router");
		    //routerEditorBot.getClass();
	  
	  }*/
}
