package org.mule.tooling.apikit.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.mule.tooling.apikit.util.APIKitProjectHelper;
import org.mule.tooling.apikit.wizard.APIkitTemplateProjectContribution;
import org.mule.tooling.core.action.ProjectLifecycleAction;
import org.mule.tooling.core.model.IMuleProject;


public class EnableApikitSupportAction implements ProjectLifecycleAction {

    @Override
    public boolean execute(IMuleProject muleProject, IProgressMonitor monitor) {
        APIKitProjectHelper apikitHelper = new APIKitProjectHelper(muleProject);
        if (apikitHelper.isAPIKitProjectEnabled()) {
            APIkitTemplateProjectContribution templateProjectContribution = new APIkitTemplateProjectContribution();
            templateProjectContribution.addContributionsTo(muleProject);
            return true;
        }
        return false;
    }
}
