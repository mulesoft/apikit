/**
 * 
 */
package org.mule.tooling.apikit.widgets;

import org.eclipse.jface.viewers.ColumnViewer;
import org.raml.model.Resource;


/**
 * Editing support for resources.
 *
 */
public class ResourceEditingSupport extends MappingEditingSupport {
    
    public ResourceEditingSupport(ColumnViewer viewer) {
        super(viewer);
    }
    
    @Override
    protected Object getValue(Object element) {
        if (element instanceof Mapping) {
            Mapping data = (Mapping) element;
            return data.getResource();
        }
        return null;
    }
    
    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof Mapping && value instanceof Resource) {
            Mapping data = (Mapping) element;
            Resource newValue = (Resource) value;
            /* only set new value if it differs from old one */
            if (!data.getResource().getRelativeUri().equals(newValue.getRelativeUri())) {
                data.setResource(newValue);
                this.getViewer().refresh();
            }
        }
    }
}
