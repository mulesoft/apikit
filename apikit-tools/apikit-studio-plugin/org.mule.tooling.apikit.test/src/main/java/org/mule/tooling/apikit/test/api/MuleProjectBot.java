package org.mule.tooling.apikit.test.api;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class MuleProjectBot {

	private SWTWorkbenchBot bot;

    public MuleProjectBot(String name, String description, SWTWorkbenchBot bot) {
        super();
        this.bot = bot;
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
    	SWTBotTreeItem item = bot.viewByTitle(explorerName).bot().tree().getTreeItem(projectName).getNode(path).getNode(yamlFileName);
    	return item.contextMenu(contextMenu).menu(generateFlowsButton).isEnabled();
    }
    
    public boolean generateFlows(String projectName,String path, String yamlFileName){
    	String explorerName = "Package Explorer";
    	String contextMenu = "APIkit";
    	String generateFlowsButton = "Generate Flows";
    	bot.viewByTitle(explorerName).bot().tree().getTreeItem(projectName).getNode(path).getNode(yamlFileName).contextMenu(contextMenu).menu(generateFlowsButton).click();
    	return true;
    }
}
