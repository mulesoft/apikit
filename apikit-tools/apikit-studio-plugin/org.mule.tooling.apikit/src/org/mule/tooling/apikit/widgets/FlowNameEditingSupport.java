/**
 * 
 */
package org.mule.tooling.apikit.widgets;

import org.eclipse.jface.viewers.ColumnViewer;
import org.mule.tooling.model.messageflow.Flow;


/**
 * Editing support for flow names.
 *
 */
public class FlowNameEditingSupport extends MappingEditingSupport {
    
    public FlowNameEditingSupport(ColumnViewer viewer) {
        super(viewer);
    }
    
    @Override
    protected Object getValue(Object element) {
        if (element instanceof Mapping) {
            Mapping data = (Mapping) element;
            return data.getFlow();
        }
        return null;
    }
    
    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof Mapping && value instanceof Flow) {
            Mapping data = (Mapping) element;
            Flow newValue = (Flow) value;
            /* only set new value if it differs from old one */
            if (!data.getFlow().getName().equals(newValue.getName())) {
                data.setFlow(newValue);
                this.getViewer().refresh();
            }
        }
    }
}
