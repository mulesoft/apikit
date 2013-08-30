/**
 * 
 */
package org.mule.tooling.apikit.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.mule.tooling.apikit.dialog.AddMappingDialog;
import org.mule.tooling.apikit.widgets.APIKitMappingCustomEditor.MappingAccesor;
import org.mule.tooling.apikit.widgets.Mapping;


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
