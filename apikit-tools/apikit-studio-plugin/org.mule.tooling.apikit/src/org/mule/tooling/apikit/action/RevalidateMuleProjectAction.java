/**
 * 
 */
package org.mule.tooling.apikit.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.mule.tooling.apikit.util.APIKitProjectHelper;
import org.mule.tooling.core.action.ProjectLifecycleAction;
import org.mule.tooling.core.model.IMuleProject;

/**
 * @author Sebastian Sampaoli
 * 
 */
public class RevalidateMuleProjectAction implements ProjectLifecycleAction {

    /**
     * 
     */
    public RevalidateMuleProjectAction() {

    }

    @Override
    public void execute(final IMuleProject muleProject, final IProgressMonitor monitor) {
        APIKitProjectHelper projectHelper = new APIKitProjectHelper(muleProject);
        projectHelper.build(monitor);
    }

}
