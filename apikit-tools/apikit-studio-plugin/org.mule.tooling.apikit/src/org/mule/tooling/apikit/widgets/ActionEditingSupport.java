/**
 * 
 */
package org.mule.tooling.apikit.widgets;

import org.eclipse.jface.viewers.ColumnViewer;
import org.raml.model.Action;


/**
 * Editing support for actions.
 *
 */
public class ActionEditingSupport extends MappingEditingSupport {
    
    public ActionEditingSupport(ColumnViewer viewer) {
        super(viewer);
    }
    
    @Override
    protected Object getValue(Object element) {
        if (element instanceof Mapping) {
            Mapping data = (Mapping) element;
            return data.getAction();
        }
        return null;
    }
    
    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof Mapping && value instanceof Action) {
            Mapping data = (Mapping) element;
            Action newValue = (Action) value;
            /* only set new value if it differs from old one */
            if (!data.getAction().equals(newValue)) {
                data.setAction(newValue);
                this.getViewer().refresh();
            }
        }
    }
}
