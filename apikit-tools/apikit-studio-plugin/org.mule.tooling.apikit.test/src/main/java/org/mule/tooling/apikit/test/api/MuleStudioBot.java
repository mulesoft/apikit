package org.mule.tooling.apikit.test.api;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForWidget;
import junit.framework.Assert;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.mule.tooling.apikit.test.api.MuleProjectBot;

public class MuleStudioBot {

    private SWTWorkbenchBot bot;
    private String projectName = "dnd" ;
    public MuleStudioBot(SWTWorkbenchBot bot) {
        super();
        this.bot = bot;

        bot.resetWorkbench();
        if (!bot.views().isEmpty())
        {
        	for(int i = 0; i < bot.views().size(); i++)
        	{
        		if (bot.views().get(i).getTitle().toString().equals((java.lang.String)"Mule Studio Welcome Page")){
		        	bot.viewByTitle("Mule Studio Welcome Page").close();
		        	changePerspective();
        			break;
        		}
        			
        	}
        }
        /*if (!bot.views().isEmpty())
        {
        	boolean active= false;
        	for(int a = 0; a > bot.views().size(); a++)
        	{
        		if (bot.views().iterator().next().isActive()){
        			active = true;
        			break;
        		}
        			
        	}
        	if (active){
		        Assert.assertNotNull("Mule Studio Welcome Page did not appear",bot.activeView());
		        if 	 (bot.activeView().getTitle().toString().equals((java.lang.String)"Mule Studio Welcome Page")){
		        	bot.viewByTitle("Mule Studio Welcome Page").close();
		        	changePerspective();
		        }
        	}
        	
        }*/
    }
    
    public void changePerspective(){
        Assert.assertNotNull("Mule perspective does not exist", bot.perspectiveByLabel("Mule"));
        bot.perspectiveByLabel("Mule").activate();
    }
    

    public MuleProjectBot createProject(String name, String description) {
    	getPrincipalShell().activate();
    	waitForWidget((withText("File")));
    	SWTBotMenu file = bot.menu("File");
        Assert.assertNotNull("File menu does not exist",file);
        SWTBotMenu newMenu = file.menu("New");
        Assert.assertNotNull("File -> New menu does not exist",newMenu);
        SWTBotMenu muleProjectMenu = newMenu.menu("Mule Project");
        Assert.assertNotNull("File -> New -> Mule Project menu does not exist",muleProjectMenu);
        muleProjectMenu.click();
        SWTBotShell muleProjectShell = bot.shell("New Mule Project");
        bot.textWithLabel("Name:").setText(name);
        projectName = name;
        bot.textWithLabel("Description:").setText(description);
        bot.button("Next >").click();
        bot.button("Next >").click();
        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(muleProjectShell));
        return new MuleProjectBot(name.toLowerCase(), description, bot);
    }
    
    public MuleProjectBot createAPIkitProject(String name, String description) {
    	getPrincipalShell().activate();
    	SWTBotMenu file = bot.menu("File");
        Assert.assertNotNull("File menu does not exist",file);
        SWTBotMenu newMenu = file.menu("New");
        Assert.assertNotNull("File -> New menu does not exist",newMenu);
        SWTBotMenu muleProjectMenu = newMenu.menu("Mule Project");
        Assert.assertNotNull("File -> New -> Mule Project menu does not exist",muleProjectMenu);
        newMenu.menu("Mule Project").click();
        SWTBotShell muleProjectShell = bot.shell("New Mule Project");
 
        bot.textWithLabel("Name:").setText(name);
        projectName = name;
        bot.textWithLabel("Description:").setText(description);
        bot.button("Next >").click();
        bot.button("Next >").click();
        SWTBotCheckBox chbox = bot.checkBox("Add APIkit components");
        Assert.assertNotNull("Add APIkit components checkbox does not exist",chbox);
        chbox.select();
        bot.button("Finish").click();

        bot.waitUntil(Conditions.shellCloses(muleProjectShell));
        return new MuleProjectBot(name.toLowerCase(), description, bot);
    }

    public APIDefinitionEditor createAPIDefinitionFile(String path, String name,String title) {
    	getPrincipalShell().activate();
    	SWTBotMenu file = bot.menu("File");
        Assert.assertNotNull("File menu does not exist",file);
        SWTBotMenu newMenu = file.menu("New");
        Assert.assertNotNull("New menu does not exist",newMenu);
        SWTBotMenu apidef = newMenu.menu("API Definition");
        Assert.assertNotNull("File - > New -> Api definition menu does not exist",apidef);
        apidef.click();
        
        SWTBotShell apidefShell = bot.shell("New API Definition File");
        Assert.assertNotNull("Api definition shell does not exist",apidefShell);
        apidefShell.activate();
		SWTBotText text1 = bot.textWithLabel("Enter or select the parent folder:");
		Assert.assertNotNull("Enter or select the parent folder: text does not exist",text1);
        text1.setText(path);
        
        SWTBotText text2 = bot.textWithLabel("File name:");
        Assert.assertNotNull("File name: text does not exist",text1);
        text2.setText(name);
		
        SWTBotText text3 = bot.textWithLabel("Title:");
        Assert.assertNotNull("Title: text does not exist",text3);
        text3.setText(title);
		
        
		SWTBotButton finishbutton = bot.button("Finish");
        Assert.assertNotNull("Finish button does not exist",finishbutton);
        Assert.assertTrue("Finish button is disabled",finishbutton.isEnabled());
        finishbutton.click();
		return new APIDefinitionEditor(bot);
    }
    
    public MuleProjectBot createAPIkitExample(String name, String description, String exampleName) {
    	getPrincipalShell().activate();
    	SWTBotMenu file = bot.menu("File");
        Assert.assertNotNull("File menu does not exist",file);
        SWTBotMenu newMenu = file.menu("New");
        Assert.assertNotNull("File -> New menu does not exist",newMenu);
        SWTBotMenu muleProjectMenu = newMenu.menu("Mule Project");
        Assert.assertNotNull("File -> New -> Mule Project menu does not exist",muleProjectMenu);
        muleProjectMenu.click();
        SWTBotShell muleProjectShell = bot.shell("New Mule Project");
        bot.textWithLabel("Name:").setText(name);
        projectName = name;
        bot.checkBox("Create project based on an existing template.").select();
        SWTBotTableItem tableItem = bot.shell("New Mule Project").bot().table(0).getTableItem(exampleName);
        Assert.assertNotNull("Example does not exist",tableItem);
        tableItem.select();
        SWTBotButton finishbutton = bot.button("Finish");
        Assert.assertNotNull("Finish button does not exist",finishbutton);
        Assert.assertTrue("Finish button is disabled",finishbutton.isEnabled());
        finishbutton.click();
        bot.waitUntil(Conditions.shellCloses(muleProjectShell));
        return new MuleProjectBot(name.toLowerCase(), description, bot);
    }
        
    public MuleStudioBot save() {
    	int i = 0;
    	for(i = 0; i < bot.shells().length; i++){
    		if (bot.shells()[i].getText().endsWith("Mule Studio")){
    			break;
    		}
    	}
    	if (bot.shells()[i].bot().menu("File").menu("Save").isEnabled()){
    		bot.shells()[i].bot().menu("File").menu("Save").click();
    	}
        return this;
    }

    public MuleStudioBot saveAll() {
    	int i = 0;
    	for(i = 0; i < bot.shells().length; i++){
    		if (bot.shells()[i].getText().endsWith("Mule Studio")){
    			break;
    		}
    	}
    	if (bot.shells()[i].activate().bot().menu("File").menu("Save All").isEnabled()){
    		bot.shells()[i].activate().bot().menu("File").menu("Save All").click();
    	}
        return this;
    }
    
    public void runApplication(){
    	SWTBotShell shell = getPrincipalShell();
    	shell.activate();
    	shell.bot().menu("Run").menu("Run").click();
		bot.sleep(5000);
    }

    public void createFlow(String flowname,String description){
    	getPrincipalShell().activate();
    	SWTBotMenu file = bot.menu("File");
        Assert.assertNotNull("File menu does not exist",file);
        SWTBotMenu newMenu = file.menu("New");
        Assert.assertNotNull("File -> New menu does not exist",newMenu);
        SWTBotMenu muleFlowMenu = newMenu.menu("Mule Flow");
        Assert.assertNotNull("File -> New -> Mule Flow menu does not exist",muleFlowMenu);
        muleFlowMenu.click();
        SWTBotShell muleProjectShell = bot.shell("New Mule Flow");
        SWTBotCombo combo = bot.comboBox(0);
        Assert.assertNotNull("Project name combo does not exist",combo);
        combo.setSelection(projectName);
        bot.textWithLabel("Name:").setText(flowname);
        bot.textWithLabel("Description:").setText(description);
        SWTBotButton finishbutton = bot.button("Finish");
        Assert.assertNotNull("Finish button does not exist",finishbutton);
        Assert.assertTrue("Finish button is disabled",finishbutton.isEnabled());
        finishbutton.click();
        bot.waitUntil(Conditions.shellCloses(muleProjectShell));
    }
    
    private SWTBotShell getPrincipalShell(){
    	SWTBotShell shell = null;
    	for(int i = 0; i < bot.shells().length; i++){
    		if (bot.shells()[i].getText().endsWith("Mule Studio")){
    			shell = bot.shells()[i]; 
    		}
    	}
    	if (shell == null){
    		Assert.assertNotNull("Mule Studio shell could not be localized",null);
    	}
    	return shell;
    }

}
