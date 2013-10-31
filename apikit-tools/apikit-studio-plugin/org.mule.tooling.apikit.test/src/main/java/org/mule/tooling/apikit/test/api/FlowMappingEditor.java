package org.mule.tooling.apikit.test.api;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

public class FlowMappingEditor {

	SWTWorkbenchBot bot;
	String WIZARD_NAME = "New Mapping";
	public FlowMappingEditor(SWTWorkbenchBot bot){
		this.bot = bot;
	}
	
	public FlowMappingEditor setResource(String resource){
		bot.shell(WIZARD_NAME).bot().comboBoxWithLabel("Resource:").setSelection(resource);
		return this;
	}
	
	public FlowMappingEditor setAction(String action){
		bot.shell(WIZARD_NAME).bot().comboBoxWithLabel("Action:").setSelection(action);
		return this;
	}
	
	public FlowMappingEditor setFlow(String flow){
		bot.shell(WIZARD_NAME).bot().comboBoxWithLabel("Flow:").setSelection(flow);
		return this;
	}
	
	public FlowMappingEditor setNewFlow(String flow){
		bot.shell(WIZARD_NAME).bot().comboBoxWithLabel("Flow:").setText(flow);
		return this;
	}
	
	public void apply(){
		bot.shell(WIZARD_NAME).bot().button("OK").click();
	}
}
