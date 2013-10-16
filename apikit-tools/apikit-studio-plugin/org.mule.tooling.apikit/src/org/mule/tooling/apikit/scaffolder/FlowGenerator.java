/**
 * 
 */
package org.mule.tooling.apikit.scaffolder;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.mule.tooling.apikit.deps.APIkitProjectClasspathRunner;
import org.mule.tooling.core.io.IMuleResources;
import org.mule.tooling.core.io.MuleResourceUtils;
import org.mule.tooling.core.model.IMuleProject;
import org.mule.tooling.messageflow.editor.MultiPageMessageFlowEditor;
import org.mule.tooling.messageflow.util.MessageFlowUtils;
import org.mule.tooling.ui.utils.UiUtils;
import org.mule.tools.apikit.ScaffolderAPI;


/**
 * API to generate flows in Studio from a Yaml file.
 * 
 * @author Sebastian Sampaoli
 *
 */
public class FlowGenerator {
    
    public void run(IProgressMonitor monitor, IProject project, final IFile ramlFile) throws CoreException {
        final IFolder appFolder = project.getFolder(IMuleResources.MULE_APP_FOLDER);
        final File file = ramlFile.getRawLocation().toFile();
        if (appFolder != null) {
            monitor.subTask("Running scaffolder...");
            new APIkitProjectClasspathRunner().run(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    ScaffolderAPI scaffolderAPI = new ScaffolderAPI(file.getParentFile(), appFolder.getRawLocation().toFile());
                    scaffolderAPI.run();
                    return null;
                }
            }, ramlFile.getProject());
            monitor.worked(1);
            project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
            monitor.worked(1);
            monitor.subTask("Updating Mule configurations...");
            updateMessageFlowEditors();
            monitor.worked(1);
        }
    }

    public void createMuleConfigs(IProgressMonitor monitor, IMuleProject muleProject) throws CoreException {
    	IProject project = muleProject.getJavaProject().getProject();
        monitor.subTask("Creating necessary Mule configurations...");
        IFolder flowsFolder = project.getFolder(IMuleResources.MULE_MESSAGE_FLOWS_FOLDER);
        IFolder muleConfigsFolder = project.getFolder(IMuleResources.MULE_APP_FOLDER);
        IResource[] members = muleConfigsFolder.members();
        String configFileName = "";
        String mFlowFileName = "";
        for (IResource configFile : members) {
            if (MuleResourceUtils.isConfigFile(configFile)){
                configFileName = FilenameUtils.removeExtension(configFile.getName());
                mFlowFileName = configFileName + "." + IMuleResources.MULE_MESSAGE_FLOW_SUFFIX;
                IFile mFlowFile = flowsFolder.getFile(mFlowFileName);
                if (!mFlowFile.exists()) {
                    UiUtils.createEmptyConfiguration(muleProject, mFlowFileName, configFileName, StringUtils.EMPTY);
                }
            }
        }
    }

    private void updateMessageFlowEditors() {
        Collection<MultiPageMessageFlowEditor> openEditors = MessageFlowUtils.getOpenMultipageMessageFlowEditors();
        for (MultiPageMessageFlowEditor messageFlowEditor : openEditors) {
            messageFlowEditor.updateFlowFromSource();
        }
    }
}
