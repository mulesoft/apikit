package org.mule.tooling.apikit.wizard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

public class NewAPIKitProjectWizard extends Wizard implements INewWizard {

    private NewAPIKitProjectWizardPage page;
    private ISelection selection;

    public NewAPIKitProjectWizard() {
        super();
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        page = new NewAPIKitProjectWizardPage(selection);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        final PojoMavenModel mavenModel = new PojoMavenModel(true, page.getVersion(), page.getGroupId(), page.getArtifactId());

        IRunnableWithProgress op = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    doFinish(mavenModel, page.getProjectName(), monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            getContainer().run(false, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error", realException.getMessage());
            return false;
        }
        return true;
    }

    /**
     * The worker method. It will find the container, create the file if missing or just replace its contents, and open the editor on the newly created file.
     * 
     * @param projectName
     * @throws URISyntaxException 
     * @throws IOException 
     */

    private void doFinish(PojoMavenModel mavenModel, String projectName, IProgressMonitor monitor) throws CoreException, URISyntaxException, IOException {
        URL resourceURL = NewAPIKitProjectWizard.class.getClassLoader().getResource("resources/sample");
        final File rootProjectFolder = new File(FileLocator.resolve(resourceURL).toURI());
        
        monitor.beginTask("Creating project " + projectName, 110);
        
        monitor.worked(10);

        if (!rootProjectFolder.exists()) {
            MessageDialog.openError(getShell(), "Error", "Project root folder does not exist.");
            return;
        }

        final File workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toFile();
        if ((new File(workspaceDir, projectName)).exists()) {
            MessageDialog.openError(getShell(), "Error", "A project with the given name already exists.");
            return;
        }
        
        final WorkspaceJob changeClasspathJob = new APIKitProjectImportJob(mavenModel, projectName, rootProjectFolder, page.getSelectedRuntimeId(), true);
        
        changeClasspathJob.schedule();

        return;
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize from it.
     * 
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        setWindowTitle("New APIKit Project");
    }
    
}