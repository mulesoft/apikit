package org.mule.tooling.apikit.test.api;

import static org.junit.Assert.assertEquals;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

public class StudioPreferencesEditor {
	private SWTWorkbenchBot bot;
	
	public StudioPreferencesEditor(SWTWorkbenchBot bot){
		this.bot = bot;
	}
	
	public void assertASRagentConfiguration(String token,String host, String port, String path){
		bot.shell("Preferences").activate();
		bot.tree(0).expandNode("Mule Studio").getNode("Anypoint Service Registry").select().click();
		assertEquals(host, bot.textWithLabel("Host").getText());
	}
	
	public void setASRagentPreferences(String token,String host, String port, String path){
		
	}
	
}