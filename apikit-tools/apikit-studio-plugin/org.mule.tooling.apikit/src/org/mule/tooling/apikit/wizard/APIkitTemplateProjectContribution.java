/**
 * 
 */
package org.mule.tooling.apikit.wizard;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.mule.tooling.apikit.Activator;
import org.mule.tooling.core.MuleCorePlugin;
import org.mule.tooling.core.model.IMuleProject;
import org.mule.tooling.core.module.ExternalContributionMuleModule;
import org.mule.tooling.core.utils.CoreUtils;

/**
 * @author Sebastian Sampaoli
 * 
 */
public class APIkitTemplateProjectContribution {

    public APIkitTemplateProjectContribution() {

    }

    public void addContributionsTo(final IMuleProject muleProject) {

        URL resourceURL = NewAPIKitProjectWizard.class.getClassLoader().getResource("resources/sample");
        try {
            final File workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toFile();
            final File newProject = new File(workspaceDir, muleProject.getName());

            // Copy src/main/api folder
            final File rootTemplateProjectFolder = new File(FileLocator.resolve(resourceURL).toURI());
            CoreUtils.copyFiles(new File(rootTemplateProjectFolder, Activator.API_FOLDER), new File(newProject, Activator.API_FOLDER));

            // Add the APIKit extension to the mule project
            final List<ExternalContributionMuleModule> externalModules = MuleCorePlugin.getModuleManager().getExternalModules();
            for (final ExternalContributionMuleModule externalContributionMuleModule : externalModules) {
                if ("Apikit".equals(externalContributionMuleModule.getName())) {
                    try {
                        muleProject.addMuleExtension(externalContributionMuleModule);
                    } catch (CoreException e) {
                        MuleCorePlugin.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not add APIKit dependencies", e));
                        e.printStackTrace();
                    }
                }
            }
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(muleProject.getName());
            project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        } catch (URISyntaxException e) {
            MuleCorePlugin.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Resource template does not exist", e));
            e.printStackTrace();
        } catch (IOException e) {
            MuleCorePlugin.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Resource template does not exist", e));
            e.printStackTrace();
        } catch (CoreException e) {
            MuleCorePlugin.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot refresh the project", e));
            e.printStackTrace();
        }
    }
}
