package org.mule.tooling.apikit.test.api;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;

public class MuleGefEditor {
	
	String flowName;
	SWTWorkbenchBot bot;
	SWTBotGefEditor editor;
	public MuleGefEditor(SWTWorkbenchBot bot, String flowName){
		this.flowName = flowName;
		this.bot = bot;
		editor = new SWTBotGefEditor(bot.editorByTitle(flowName).getReference(), bot);
	}
	
	
	public void changeTab(String tabName){
		bot.editorByTitle(flowName).bot().cTabItem(tabName).activate();
	}
	
	public String getTextOfTheTab(){
		return bot.editorByTitle(flowName).toTextEditor().getText();
	}
	
	public void setTextOfTheTab(String text){
		bot.editorByTitle(flowName).toTextEditor().setText(text);
	}
	
	public void save(){
		bot.editorByTitle(flowName).toTextEditor().save();
	}
	
	public void clickOnAbox(String boxName){
	  	editor.doubleClick(boxName);
	}
	
	public void filterAndDragNdrop(String component, int x, int y){
		editor.bot().textWithLabel("Filter:").setText(component);
        editor.activateTool(component).click(x,y);
	}
	

}
