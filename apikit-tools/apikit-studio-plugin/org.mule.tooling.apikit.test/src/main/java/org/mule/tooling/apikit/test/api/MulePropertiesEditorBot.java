package org.mule.tooling.apikit.test.api;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;

public class MulePropertiesEditorBot {

    private SWTWorkbenchBot bot;
    private SWTBot editorBot;

    public MulePropertiesEditorBot(SWTWorkbenchBot bot) {
        this.bot = bot;
        SWTBotView viewById = getViewBot();
        viewById.show();
        viewById.setFocus();
        editorBot = viewById.bot();
    }
    
	public MulePropertiesEditorBot activate(){
    	SWTBotView viewById = getViewBot();
        viewById.show();
        viewById.setFocus();
        return this;
    }

    protected SWTBotView getViewBot() {
        return bot.viewById("org.mule.tooling.properties.views.MulePropertiesView");
    }

    public MulePropertiesEditorBot setTextValue(String label, String value) {
        editorBot.textWithLabel(label).setText(value);
        return this;
    }
    
    public MulePropertiesEditorBot selectComboBox(int comboId, String selection){
    	getViewBot().bot().comboBox(comboId).setSelection(selection);
    	return this;
    }
    /*
    public MuleGlobalElementWizardEditorBot clickTooltipButton(String tooltip){
    	activate();
    	editorBot.toolbarButtonWithTooltip(tooltip).click();
    	return new MuleGlobalElementWizardEditorBot(bot, this);
    }*/
    
    public void clickTooltipButton(String tooltip){
    	activate();
    	editorBot.toolbarButtonWithTooltip(tooltip).click();
    }

    public String getTextValue(String label) {
    	return editorBot.textWithLabel(label).getText();
    }
    
    public void apply() {
    	for(int a = 0; a < getViewBot().getToolbarButtons().size(); a++){
    		if (getViewBot().getToolbarButtons().get(a).getToolTipText().equals((java.lang.String)"Apply")){
    			getViewBot().getToolbarButtons().get(a).click();
    			break;
    		}
    	}
    }
    
    public void close(){
    	editorBot.activeShell().close();
    }
    
}
