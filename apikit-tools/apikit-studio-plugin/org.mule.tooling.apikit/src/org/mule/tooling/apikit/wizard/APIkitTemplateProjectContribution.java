/**
 * 
 */
package org.mule.tooling.apikit.wizard;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.mule.tooling.apikit.Activator;
import org.mule.tooling.apikit.util.APIKitProjectHelper;
import org.mule.tooling.core.MuleCorePlugin;
import org.mule.tooling.core.model.IMuleProject;
import org.mule.tooling.core.utils.CoreUtils;

/**
 * @author Sebastian Sampaoli
 * 
 */
public class APIkitTemplateProjectContribution {

    public APIkitTemplateProjectContribution() {

    }

    public void addContributionsTo(final IMuleProject muleProject) {
        try {
            final File workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toFile();
            final File newProject = new File(workspaceDir, muleProject.getName());

            // Copy src/main/api folder if it is necessary
            APIKitProjectHelper projectHelper = new APIKitProjectHelper(muleProject);
            if (!projectHelper.isAPIkitFromRAMLFile()) {
                URL resourceURL = APIkitTemplateProjectContribution.class.getClassLoader().getResource(Activator.EXAMPLE_PROJECT_ROOT);
                final File rootTemplateProjectFolder = new File(FileLocator.resolve(resourceURL).toURI());
                CoreUtils.copyFiles(new File(rootTemplateProjectFolder, Activator.API_FOLDER), new File(newProject, Activator.API_FOLDER));
                CoreUtils.copyFiles(new File(rootTemplateProjectFolder, Activator.RESOURCES_FOLDER), new File(newProject, Activator.RESOURCES_FOLDER));
            }
            // Add the APIKit extension to the mule project
            projectHelper.addAPIkitExtension();
            
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(muleProject.getName());
            project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        } catch (URISyntaxException e) {
            MuleCorePlugin.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Resource template does not exist", e));
        } catch (IOException e) {
            MuleCorePlugin.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Resource template does not exist", e));
        } catch (CoreException e) {
            MuleCorePlugin.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot refresh the project", e));
        }
    }
}
