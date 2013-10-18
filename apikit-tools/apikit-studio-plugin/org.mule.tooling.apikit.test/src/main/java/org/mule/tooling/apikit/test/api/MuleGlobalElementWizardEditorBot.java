package org.mule.tooling.apikit.test.api;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForWidget;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;



public class MuleGlobalElementWizardEditorBot {

    private SWTWorkbenchBot bot;
    private MulePropertiesEditorBot parent;

    public MuleGlobalElementWizardEditorBot(SWTWorkbenchBot bot, MulePropertiesEditorBot parent) {
        this.bot = bot;
        this.parent = parent;
        waitForWidget((withText("Global Element Properties")));

        bot.shell("Global Element Properties").activate();
        bot.shell("Global Element Properties").setFocus();
    }
    
    public MuleGlobalElementWizardEditorBot setTextValue(String label, String value) {
        bot.textWithLabel(label).setText(value);
        return this;
    }
    
    public MulePropertiesEditorBot clickButton(String buttonText){
    	bot.button(buttonText).click();
    	return parent;
    }
}