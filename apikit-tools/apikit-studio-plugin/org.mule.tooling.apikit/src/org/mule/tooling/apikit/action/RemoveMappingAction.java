/**
 * 
 */
package org.mule.tooling.apikit.action;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.mule.tooling.apikit.widgets.APIKitMappingCustomEditor.MappingAccesor;
import org.mule.tooling.apikit.widgets.Mapping;


/**
 * Remove a mapping from the mappings table viewer.
 * 
 */
public class RemoveMappingAction extends Action {

    private static final String REMOVE_MAPPING = "Remove mapping";
    private static final String REMOVE_MAPPING_TOOLTIP = "Remove the current selected mapping";
    private MappingAccesor mappingAccesor;
    
    public RemoveMappingAction(MappingAccesor mappingAccesor) {
        setId(REMOVE_MAPPING);
        setToolTipText(REMOVE_MAPPING_TOOLTIP);
        setText(REMOVE_MAPPING);
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
        setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
        this.mappingAccesor = mappingAccesor;
    }

    
    @Override
    public boolean isEnabled() {
        if (mappingAccesor.getViewer() != null) {
            return mappingAccesor.getSelectedMapping() != null;
        }
        return true;
    }
    
    @Override
    public void run() {
        Mapping selectedMapping = mappingAccesor.getSelectedMapping();
        mappingAccesor.removeMapping(selectedMapping);
    }
}
