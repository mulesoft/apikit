package org.mule.tooling.apikit.widgets;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class MappingsLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        Mapping mapping = (Mapping) element;
        switch (columnIndex) {
        case 0:
            return mapping.getResource().getUri();
        case 1:
            String name = mapping.getAction().getType().name();
            String actionName = name.substring(0, 1) + name.substring(1, name.length()).toLowerCase();
            return actionName;
        case 2:
            return mapping.getFlow().getName();
        default:
            return "";
        }
    }

}
