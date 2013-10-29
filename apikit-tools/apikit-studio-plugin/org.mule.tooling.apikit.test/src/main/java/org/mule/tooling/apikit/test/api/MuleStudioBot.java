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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
public class MuleStudioBot {

    private SWTWorkbenchBot bot;
    private String projectName = "dnd" ;
    public MuleStudioBot(SWTWorkbenchBot bot) {
        super();
        this.bot = bot;

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
    }
    
    public void changePerspective(){
        Assert.assertNotNull("Mule perspective does not exist", bot.perspectiveByLabel("Mule"));
        bot.perspectiveByLabel("Mule").activate();
    }
    
    public MuleStudioBot createProject(String name, String description,String muleVersion) {
    	getPrincipalShell().activate();
    	waitForWidget((withText("File")));
    	SWTBotMenu file = bot.menu("File");
        SWTBotMenu newMenu = file.menu("New");
        SWTBotMenu muleProjectMenu = newMenu.menu("Mule Project");
        muleProjectMenu.click();
        SWTBotShell muleProjectShell = bot.shell("New Mule Project");
        bot.textWithLabel("Name:").setText(name);
        projectName = name;
        bot.textWithLabel("Description:").setText(description);
        bot.comboBoxWithLabel("Server Runtime:").setSelection(muleVersion);
        bot.button("Next >").click();
        bot.button("Next >").click();
        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(muleProjectShell));
        return this;
    }
    
    public MuleStudioBot createAPIkitProject(String name, String description, String muleVersion) {
    	getPrincipalShell().activate();
    	SWTBotMenu file = bot.menu("File");
        SWTBotMenu newMenu = file.menu("New");
        SWTBotMenu muleProjectMenu = newMenu.menu("Mule Project");
        muleProjectMenu.click();
        SWTBotShell muleProjectShell = bot.shell("New Mule Project");
 
        bot.textWithLabel("Name:").setText(name);
        projectName = name;
        bot.textWithLabel("Description:").setText(description);
        bot.comboBoxWithLabel("Server Runtime:").setSelection(muleVersion);
        bot.button("Next >").click();
        bot.button("Next >").click();
        SWTBotCheckBox chbox = bot.checkBox("Add APIkit components");
        Assert.assertNotNull("Add APIkit components checkbox does not exist",chbox);
        chbox.select();
        bot.button("Finish").click();

        bot.waitUntil(Conditions.shellCloses(muleProjectShell));
        return this;
    }

    public APIDefinitionEditor createAPIDefinitionFile(String path, String name,String title) {
    	getPrincipalShell().activate();
    	SWTBotMenu file = bot.menu("File");
        SWTBotMenu newMenu = file.menu("New");
        SWTBotMenu apidef = newMenu.menu("API Definition");
        apidef.click();
        
        SWTBotShell apidefShell = bot.shell("New API Definition File");
        apidefShell.activate();
		SWTBotText text1 = bot.textWithLabel("Enter or select the parent folder:");
        text1.setText(path);
        
        SWTBotText text2 = bot.textWithLabel("File name:");
        text2.setText(name);
		
        SWTBotText text3 = bot.textWithLabel("Title:");
        text3.setText(title);
		
        
		SWTBotButton finishbutton = bot.button("Finish");
        finishbutton.click();
		return new APIDefinitionEditor(bot);
    }
    
    public MuleStudioBot createAPIkitExample(String name, String description, String exampleName) {
    	getPrincipalShell().activate();
    	SWTBotMenu file = bot.menu("File");
        SWTBotMenu newMenu = file.menu("New");
        SWTBotMenu muleProjectMenu = newMenu.menu("Mule Project");
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
        return this;
    }
        
    public MuleStudioBot save(){
    	int i = 0;
    	for(i = 0; i < bot.shells().length; i++){
    		if (bot.shells()[i].getText().endsWith("Mule Studio")){
    			break;
    		}
    	}
    	if (bot.shells()[i].activate().bot().menu("File") == null){
    		waitForWidget(withText("File")); 
    	}
    	if (bot.shells()[i].activate().bot().menu("File").menu("Save").isEnabled()){
    		bot.shells()[i].activate().bot().menu("File").menu("Save").click();
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
    	
    	if (bot.shells()[i].activate().bot().menu("File") == null){
    		waitForWidget(withText("File")); 
    	}
    	if (bot.shells()[i].activate().bot().menu("File").menu("Save All").isEnabled()){
    		bot.shells()[i].activate().bot().menu("File").menu("Save All").click();
    	}
    	//}
        return this;
    }
    
    public MuleStudioBot closeAll() {
    	int i = 0;
    	for(i = 0; i < bot.shells().length; i++){
    		if (bot.shells()[i].getText().endsWith("Mule Studio")){
    			break;
    		}
    	}
    	if (bot.shells()[i].activate().bot().menu("File").menu("Close All").isEnabled()){
    		bot.shells()[i].activate().bot().menu("File").menu("Close All").click();
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
        SWTBotMenu newMenu = file.menu("New");
        SWTBotMenu muleFlowMenu = newMenu.menu("Mule Flow");
        muleFlowMenu.click();
        SWTBotShell muleProjectShell = bot.shell("New Mule Flow");
        SWTBotCombo combo = bot.comboBox(0);
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

    protected SWTBotTreeItem getProjectTree(String projectName) {
        bot.viewByTitle("Package Explorer").setFocus();
        SWTBotTreeItem projectTree = bot.tree().getTreeItem(projectName);
        return projectTree;
    }
    
    public boolean canGenerateFlows(String projectName,String path, String yamlFileName){
    	String explorerName = "Package Explorer";
    	String contextMenu = "APIkit";
    	String generateFlowsButton = "Generate Flows";
    	
    	bot.viewByTitle(explorerName).show();
    	bot.viewByTitle(explorerName).setFocus();

    	SWTBotTreeItem item = bot.viewByTitle(explorerName).bot().tree().getTreeItem(projectName).getNode(path).getNode(yamlFileName);
    	return item.contextMenu(contextMenu).menu(generateFlowsButton).isEnabled();

    }
    
    public boolean generateFlows(String projectName,String path, String yamlFileName){
    	String explorerName = "Package Explorer";
    	String contextMenu = "APIkit";
    	String generateFlowsButton = "Generate Flows";
    	bot.viewByTitle(explorerName).show();
    	bot.viewByTitle(explorerName).setFocus();
    	bot.viewByTitle(explorerName).bot().tree().getTreeItem(projectName).getNode(path).getNode(yamlFileName).contextMenu(contextMenu).menu(generateFlowsButton).click();
    	return true;
    }
}
