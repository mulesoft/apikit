package org.mule.tooling.apikit.test;


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mule.tooling.apikit.test.api.APIDefinitionEditor;
import org.mule.tooling.apikit.test.api.MuleStudioBot;
import org.mule.tooling.apikit.test.api.UILabels;
import org.mule.tooling.apikit.test.api.XmlComparer;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ScaffolderTests {

    private static SWTWorkbenchBot bot;
    private static MuleStudioBot muleStudioBot;
    
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        // increase timeout to 8 seconds
        SWTBotPreferences.TIMEOUT = 8000;
        // increase timeout to 1 second
        SWTBotPreferences.PLAYBACK_DELAY = 10;
        bot = new SWTWorkbenchBot();
        bot.resetWorkbench();
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
	  public void createSimpleExampleUsingScaffolderCompareXMLmule34() throws Exception{
		  createSimpleExampleUsingScaffolderCompareXML(UILabels.MULE_34,"Mule34");
	  }
	  

	  @Test
	  public void createSimpleExampleUsingScaffolderCompareXMLmule35() throws Exception{
		  createSimpleExampleUsingScaffolderCompareXML(UILabels.MULE_35,"Mule35");
	  }
		  
     public void createSimpleExampleUsingScaffolderCompareXML(String muleVersion,String projectNameAddition) throws Exception{
		  	final String yamlFileInput = "resources/createSimpleExampleUsingScaffolderCompareXML-input.yaml";
		  	final String xmlFileExpected = "/resources/createSimpleExampleUsingScaffolderCompareXML" + projectNameAddition + "-expected.xml";
			final String projectName = "cseuscx"+ System.currentTimeMillis();
			final String flowName = "simpleyamlfile" + projectNameAddition;
		  	final String yamlFilePath = "src/main/api";
		  	final String yamlFileName = flowName + ".yaml";
		  	
		  	final MuleStudioBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description",muleVersion);
		  	final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
		  	apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
		  	
		  	assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
		  	
		  	projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);

		  	XmlComparer comparer = new XmlComparer(bot);
	        comparer.compareToTheXMLUsingUI("XML files are different.",flowName, xmlFileExpected,true);
	        muleStudioBot.saveAll();
	  }

	  @Test
	  public void createLeaguesExampleUsingScaffolderMule34() throws Exception{
		  createLeaguesExampleUsingScaffolder(UILabels.MULE_34, "Mule34");
	  }
	  
	  @Test
	  public void createLeaguesExampleUsingScaffolderMule35() throws Exception{
		  createLeaguesExampleUsingScaffolder(UILabels.MULE_35, "Mule35");
	  }
		  
	  public void createLeaguesExampleUsingScaffolder(String muleVersion,String projectNameAddition) throws Exception{

			final String yamlFileInput = "resources/createLeaguesExampleUsingScaffolder-input.yaml";
			final String xmlFileExpected = "/resources/createLeaguesExampleUsingScaffolder"+ projectNameAddition +"-expected.xml";
			final String projectName = "cleus"+ System.currentTimeMillis();
			final String flowName = "leaguesyamlfile"+ projectNameAddition;
			final String yamlFilePath = "src/main/api";
			final String yamlFileName = flowName  + ".yaml";
			
			final MuleStudioBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description",muleVersion);
			final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
			apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
			assertTrue("Cannot generate flows due to invalid yaml file.",projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
			
			projectBot.generateFlows(projectName,yamlFilePath, yamlFileName);
			
			XmlComparer comparer = new XmlComparer(bot);
			comparer.compareToTheXMLUsingUI("XML files are different.",flowName, xmlFileExpected,true);
			muleStudioBot.saveAll();
	  }

	   @Test
	   public void createSalesInvalidExampleUsingScaffolderMule34() throws Exception{
		   createSalesInvalidExampleUsingScaffolder(UILabels.MULE_34, "Mule34");
	   }
	   
	   @Test
	   public void createSalesInvalidExampleUsingScaffolderMule35() throws Exception{
		   createSalesInvalidExampleUsingScaffolder(UILabels.MULE_35, "Mule35");
	   }
	  
	  public void createSalesInvalidExampleUsingScaffolder(String muleVersion,String projectNameAddition) throws Exception{
		  final String yamlFileInput = "resources/createSalesInvalidExampleUsingScaffolder-input.yaml";
		  final String projectName = "csieuscx"+ System.currentTimeMillis();

			final String yamlFilePath = "src/main/api";
			final String yamlFileName = "invalid" + projectNameAddition + "-input.yaml";
			
			final MuleStudioBot projectBot = muleStudioBot.createAPIkitProject(projectName, "this is a description",muleVersion);
			final APIDefinitionEditor apiDefinitionEditor = muleStudioBot.createAPIDefinitionFile(projectName +"/"+ yamlFilePath,yamlFileName,"title");
			apiDefinitionEditor.completeYamlFile(yamlFileInput).save();
			assertTrue("This example should not be valid.",!projectBot.canGenerateFlows(projectName,yamlFilePath, yamlFileName));
			
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
