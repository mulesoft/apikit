/**
 * 
 */
package org.mule.tooling.apikit.util;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.mule.tooling.apikit.Activator;
import org.mule.tooling.core.MuleCorePlugin;
import org.mule.tooling.core.model.IMuleProject;
import org.osgi.service.prefs.BackingStoreException;


/**
 * @author Sebastian Sampaoli
 *
 */
public class APIKitProjectHelper {
    
    private static final boolean APIKIT_ENABLED_DEFAULT = false;
    public static final String PREFERENCE_KEY_APIKIT_ENABLED = "apikitEnabled";
    private IEclipsePreferences preferenceNode;
    private IMuleProject muleProject;

    public APIKitProjectHelper(IMuleProject muleProject) {
        this.muleProject = muleProject;
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
}
