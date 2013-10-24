/**
 * 
 */
package org.mule.tooling.apikit.action;

import java.util.concurrent.Callable;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.mule.tooling.core.action.ProjectLifecycleAction;
import org.mule.tooling.core.model.IMuleProject;
import org.mule.tooling.ui.widgets.util.SilentRunner;

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
        SilentRunner.run(new Callable<Void>() {

            @Override
            public Void call() throws Exception {                
                muleProject.getJavaProject().getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                return null;
            }
        }, null);

    }

}
