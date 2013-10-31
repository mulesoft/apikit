package org.mule.tooling.apikit.test.api;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForWidget;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;



public class GlobalElementWizardEditorBot {

    private SWTWorkbenchBot bot;
    private static String WIZARD_TITLE = "Global Element Properties";
    
    public GlobalElementWizardEditorBot(SWTWorkbenchBot bot) {
        this.bot = bot;
        waitForWidget((withText(WIZARD_TITLE)));
        activate();
        
    }
    private void activate(){
    	bot.shell(WIZARD_TITLE).activate();
        bot.shell(WIZARD_TITLE).setFocus();
    }
    
    public GlobalElementWizardEditorBot setTextValue(String label, String value) {
        bot.textWithLabel(label).setText(value);
        return this;
    }
    
    public GlobalElementWizardEditorBot setYamlFileName(String value){
    	activate();
    	bot.textWithLabel("YAML File:").setText(value);
        return this;
    }
    
    public GlobalElementWizardEditorBot setConsolePath(String value){
    	activate();
    	bot.textWithLabel("Console Path:").setText(value);
        return this;
    }
    
    public GlobalElementWizardEditorBot setName(String value){
    	activate();
    	bot.textWithLabel("Name:").setText(value);
        return this;
    }
    
    public void clickOK(){
    	activate();
    	bot.button("OK").click();
    }
    
    public void clickOnAddAnewMapping(){
		activate();
		bot.toolbarButtonWithTooltip("Add a new mapping").click();
	}
    
    public void clickOnRemoveCurrentMapping(){
    	activate();
		bot.toolbarButtonWithTooltip("Remove the current selected mapping").click();
    }
    
	public String getTableText(int row, int column){
		return bot.shell(WIZARD_TITLE).activate().bot().table(0).cell(row,column).toString();
	}
    
}