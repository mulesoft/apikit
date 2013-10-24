package org.mule.tooling.apikit.test;


import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mule.tooling.apikit.test.api.APIDefinitionEditor;
import org.mule.tooling.apikit.test.api.MuleProjectBot;
import org.mule.tooling.apikit.test.api.MuleStudioBot;
import org.mule.tooling.apikit.test.api.XmlComparer;

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

    public void cleanProjects(){
    	SWTBotTreeItem[] projects = bot.viewByTitle("Package Explorer").bot().tree().getAllItems();
    	for(int i = 0; i< projects.length ; i++){
    		projects[i].contextMenu("Delete").click();
    		 bot.shell("Delete Resources").activate();
    		 bot.button("OK").click();
    	}
    }
	
	  @Test
	  public void createSimpleExampleUsingScaffolderCompareXML() throws Exception{
		  	final String yamlFileInput = "resources/new-file-input.yaml";
		  	final String xmlFileExpected = "/resources/apikit-simple-test-expected.xml";
			final String projectName = "cseuscx"+ System.currentTimeMillis();
			final String flowName = "simpleYamlFile";
		  	final String yamlFilePath = "src/main/api";
		  	final String yamlFileName = flowName + ".yaml";
		  	
		  	final MuleProjectBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description");
		  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
		  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
		  	
		  	assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
		  	
		  	projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);

		  	XmlComparer comparer = new XmlComparer(bot);
	        comparer.compareToTheXMLUsingUI("XML files are different.",flowName, xmlFileExpected,true);
	        muleStudioBot.saveAll();
	  }

	  @Test
	  public void createLeaguesExampleUsingScaffolder() throws Exception{
			final String yamlFileInput = "resources/leagues-input.yaml";
			final String xmlFileExpected = "/resources/apikit-leagues-example-expected.xml";
			final String projectName = "cleus"+ System.currentTimeMillis();
			final String flowName = "leaguesYamlFile";
			final String yamlFilePath = "src/main/api";
			final String yamlFileName = flowName  + ".yaml";
			
			final MuleProjectBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description");
			final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
			apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
			assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
			
			projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);
			
			XmlComparer comparer = new XmlComparer(bot);
			comparer.compareToTheXMLUsingUI("XML files are different.",flowName, xmlFileExpected,true);
			muleStudioBot.saveAll();
	  }
	  
	   @Test
	  public void createSalesInvalidExampleUsingScaffolderCompareXML() throws Exception{
		  final String yamlFileInput = "resources/invalid-input.yaml";
		  final String projectName = "csieuscx"+ System.currentTimeMillis();

			final String yamlFilePath = "src/main/api";
			final String yamlFileName = "invalidYamlFile.yaml";
			
			final MuleProjectBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description");
			final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
			apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
			assertTrue("This example should not be valid.",!projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
			
	  }
	   
	   
	   
	   
}
