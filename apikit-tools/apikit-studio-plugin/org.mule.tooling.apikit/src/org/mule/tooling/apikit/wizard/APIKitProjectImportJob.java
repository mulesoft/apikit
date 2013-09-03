package org.mule.tooling.apikit.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.mule.tooling.core.packageManager.ImportPackageManager;
import org.mule.tooling.ui.utils.UiUtils;

public class APIKitProjectImportJob extends WorkspaceJob {

    private String projectName;
    private File rootFile;
    private String runtimeId;
    private boolean copyToWorkspace;
    private PojoMavenModel mavenModel;

    public APIKitProjectImportJob(PojoMavenModel mavenModel, final String projectName, final File rootFile, final String runtimeId, boolean copyToWorkspace) {
        super("APIKit Import");
        this.projectName = projectName;
        this.rootFile = rootFile;
        this.runtimeId = runtimeId;
        this.copyToWorkspace = copyToWorkspace;
        this.mavenModel = mavenModel;

        // setUser(true);
        // setRule(ResourcesPlugin.getWorkspace().getRoot()); // this locks the Workspace and makes everything work strangely, do not use.
        addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                UiUtils.showFirstFlowInProject(projectName);
            }
        });
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
        try {
            final ImportPackageManager importPackageManager = new ImportAPIKitProjectManager(mavenModel, projectName, rootFile, runtimeId, copyToWorkspace);
            importPackageManager.run(monitor);
        } catch (InvocationTargetException e) {
            reportMessage(e);
            return Status.CANCEL_STATUS;
        } catch (InterruptedException e) {
            reportMessage(e);
            return Status.CANCEL_STATUS;
        }

        return Status.OK_STATUS;
    }

    /**
     * Reports a Dialog (in the UI thread)
     * 
     * @param e
     */
    private void reportMessage(final Throwable e) {
        Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                final Throwable cause = e.getCause();
                MessageDialog.openError(null, "Error", "Unable to create or initialize project.\n" + (cause != null ? cause.getMessage() : e.getMessage()));
            }
        });
    }
}
