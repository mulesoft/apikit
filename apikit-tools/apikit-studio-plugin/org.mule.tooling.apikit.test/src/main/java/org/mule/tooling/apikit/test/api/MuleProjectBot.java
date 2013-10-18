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

    
    public MuleProjectBot generateFlows(String projectName,String path, String yamlFileName){
  		bot.viewByTitle("Package Explorer").bot().tree().getTreeItem(projectName).getNode(path).getNode(yamlFileName).contextMenu("Generate Flows").click();
    		return this;
    }
}
