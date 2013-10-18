package org.mule.tooling.apikit.test.api;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;

public class APIDefinitionEditor {

	public SWTWorkbenchBot bot;
	public SWTBotEditor editor;
	
	public APIDefinitionEditor(SWTWorkbenchBot bot){
		this.bot = bot;
		editor = bot.activeEditor();
	}
	
	
	public APIDefinitionEditor completeYamlFile(String yamlFileName) throws IOException
	  {
		  String file = readResource(yamlFileName);
		  editor.toTextEditor().setText(file);
		  return this;
	  }
	
	public APIDefinitionEditor save(){
		editor.saveAndClose();
		return this;
	}
	  
    protected String readResource(String configName) throws IOException {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(configName);
        return IOUtils.toString(resourceAsStream);
    }
}
