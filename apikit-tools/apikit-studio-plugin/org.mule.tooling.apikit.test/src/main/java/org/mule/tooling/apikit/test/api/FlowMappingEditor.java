package org.mule.tooling.apikit.test.api;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

public class FlowMappingEditor {

	SWTWorkbenchBot bot;
	public FlowMappingEditor(SWTWorkbenchBot bot){
		this.bot = bot;
	}
	
	public FlowMappingEditor setResource(String resource){
		bot.shell("New Mapping").bot().comboBoxWithLabel("Resource:").setSelection(resource);
		return this;
	}
	
	public FlowMappingEditor setAction(String action){
		bot.shell("New Mapping").bot().comboBoxWithLabel("Action:").setSelection(action);
		return this;
	}
	
	public FlowMappingEditor setFlow(String flow){
		bot.shell("New Mapping").bot().comboBoxWithLabel("Flow:").setSelection(flow);
		return this;
	}
	
	public void apply(){
		bot.shell("New Mapping").bot().button("OK").click();
	}
}
