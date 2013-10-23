package org.mule.tooling.apikit.test.api;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

public class APIkitRouterFlowMappingEditor {

	SWTWorkbenchBot bot;
	public APIkitRouterFlowMappingEditor(SWTWorkbenchBot bot){
		this.bot = bot;
	}
	
	public APIkitRouterFlowMappingEditor setResource(String resource){
		bot.shell("New Mapping").bot().textWithLabel("Resource:").setText(resource);
		return this;
	}
	
	public APIkitRouterFlowMappingEditor setAction(String action){
		bot.shell("New Mapping").bot().comboBox("Action:").setSelection(action);
		return this;
	}
	
	public APIkitRouterFlowMappingEditor setFlow(String flow){
		bot.shell("New Mapping").bot().textWithLabel("Flow:").setText(flow);
		return this;
	}
	
	public void apply(){
		bot.shell("New Mapping").bot().button("OK").click();
	}
}
