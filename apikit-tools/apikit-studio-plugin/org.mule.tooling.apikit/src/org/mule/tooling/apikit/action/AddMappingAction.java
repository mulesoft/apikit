/**
 * 
 */
package org.mule.tooling.apikit.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.mule.tooling.apikit.dialog.AddMappingDialog;
import org.mule.tooling.apikit.widgets.APIKitMappingCustomEditor.MappingAccesor;
import org.mule.tooling.apikit.widgets.Mapping;
import org.mule.tooling.core.MuleCorePlugin;


/**
 * Add a new mapping to the mappings table viewer.
 * 
 */
public class AddMappingAction extends Action {

    private static final String ADD_MAPPING = "Add a new mapping";
    private MappingAccesor mappingAccessor;
    
    public AddMappingAction(MappingAccesor mappingAccessor) {
        setId(ADD_MAPPING);
        setToolTipText(ADD_MAPPING);
        setText(ADD_MAPPING);
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
        this.mappingAccessor = mappingAccessor;
    }

    
    @Override
    public boolean isEnabled() {
        if (mappingAccessor != null) {
            IFile ramlFile = mappingAccessor.retrieveRamlFile();
            Shell activeShell = Display.getCurrent().getActiveShell();
            if (ramlFile != null) {
                if (ramlFile.exists()) {
                    try {
                        mappingAccessor.retrieveRamlSpec();
                        return true;
                    } catch (Exception ex) {
                        MessageDialog.open(MessageDialog.ERROR, activeShell, "Could not add flow mapping", "The file " + ramlFile.getName() + " is not a valid RAML file. Please, specify a valid RAML file or leave the YAML File field empty. This last option is useful when a YAML file does not exist yet.", SWT.NONE);
                        MuleCorePlugin.logError("Could not add flow mapping, The file " + ramlFile.getName() + " is not a valid RAML file", ex);
                        return false;
                    }
                }
                MessageDialog.open(MessageDialog.ERROR, activeShell, "Could not add flow mapping", "The file " + ramlFile.getName() + " does not exist. Please, specify an existing YAML file or leave the YAML File field empty. This last option is useful when a YAML file does not exist yet.", SWT.NONE);
                MuleCorePlugin.logInfo("Could not add flow mapping, The file " + ramlFile.getName() + " does not exist");
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void run() {
        AddMappingDialog addMappingDialog = new AddMappingDialog(Display.getCurrent().getActiveShell(), mappingAccessor);
        int open = addMappingDialog.open();
        if (open == IDialogConstants.OK_ID) {
            Mapping mapping = addMappingDialog.getGeneratedMapping();
            mappingAccessor.addMapping(mapping);
        }
    }
}
