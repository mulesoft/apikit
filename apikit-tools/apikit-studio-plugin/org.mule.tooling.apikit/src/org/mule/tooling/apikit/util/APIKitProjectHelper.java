/**
 * 
 */
package org.mule.tooling.apikit.util;

import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.mule.tooling.apikit.Activator;
import org.mule.tooling.core.MuleCorePlugin;
import org.mule.tooling.core.model.IMuleProject;
import org.mule.tooling.core.module.ExternalContributionMuleModule;
import org.mule.tooling.ui.widgets.util.SilentRunner;
import org.osgi.service.prefs.BackingStoreException;


/**
 * @author Sebastian Sampaoli
 *
 */
public class APIKitProjectHelper {
    
    private static final String APIKIT_PLUGIN_ID = "APIkit";
    private static final boolean APIKIT_ENABLED_DEFAULT = false;
    public static final String PREFERENCE_KEY_APIKIT_ENABLED = "apikitEnabled";
    private static final boolean APIKIT_FROM_RAML_DEFAULT = false;
    public static final String PREFERENCE_KEY_APIKIT_FROM_RAML = "apikitFromRaml";
    
    private IEclipsePreferences preferenceNode;
    private IMuleProject muleProject;

    public APIKitProjectHelper(IMuleProject muleProject) {
        this.muleProject = muleProject;
    }
    
    public boolean isAPIkitFromRAMLFile() {
        IEclipsePreferences preferenceNode = getPreferenceNode();
        return preferenceNode.getBoolean(PREFERENCE_KEY_APIKIT_FROM_RAML, APIKIT_FROM_RAML_DEFAULT);
    }
    
    public void setAPIKitProjectFromRaml(boolean enabled) {
        IEclipsePreferences preferenceNode = getPreferenceNode();
        preferenceNode.putBoolean(PREFERENCE_KEY_APIKIT_FROM_RAML, enabled);
        try {
            preferenceNode.flush();
        } catch (BackingStoreException e) {
            MuleCorePlugin.logError("Problem storing the project's APIKit support preferences", e);
        }
    }
    
    public boolean isAPIKitProjectEnabled() {
        IEclipsePreferences preferenceNode = getPreferenceNode();
        return preferenceNode.getBoolean(PREFERENCE_KEY_APIKIT_ENABLED, APIKIT_ENABLED_DEFAULT);
    }

    public void setAPIKitProjectEnabled(boolean enabled) {
        IEclipsePreferences preferenceNode = getPreferenceNode();
        preferenceNode.putBoolean(PREFERENCE_KEY_APIKIT_ENABLED, enabled);
        try {
            preferenceNode.flush();
        } catch (BackingStoreException e) {
            MuleCorePlugin.logError("Problem storing the project's APIKit support preferences", e);
        }
    }

    private IEclipsePreferences getPreferenceNode() {
        if (preferenceNode == null) {
            ProjectScope projectScope = new ProjectScope(muleProject.getJavaProject().getProject());
            preferenceNode = projectScope.getNode(Activator.PLUGIN_ID);
        }
        return preferenceNode;
    }
    
    public void addAPIkitExtension() {
    	final List<ExternalContributionMuleModule> externalModules = MuleCorePlugin.getModuleManager().getExternalModules();
        for (final ExternalContributionMuleModule externalContributionMuleModule : externalModules) {
            if (APIKIT_PLUGIN_ID.equals(externalContributionMuleModule.getName())) {
                try {
                    muleProject.addMuleExtension(externalContributionMuleModule);
                } catch (CoreException e) {
                    MuleCorePlugin.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not add APIKit dependencies", e));
                }
            }
        }
    }

    public void build(final IProgressMonitor monitor) {
    	SilentRunner.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {                
                muleProject.getJavaProject().getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                return null;
            }
        }, null);
    }

	public void refresh() {
		SilentRunner.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {                
            	muleProject.getJavaProject().getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
                return null;
            }
        }, null);
	}
}
