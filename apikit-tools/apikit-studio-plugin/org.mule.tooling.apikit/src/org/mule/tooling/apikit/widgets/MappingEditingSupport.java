package org.mule.tooling.apikit.widgets;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.mule.tooling.model.messageflow.Flow;
import org.raml.model.Action;
import org.raml.model.Resource;


public abstract class MappingEditingSupport extends EditingSupport {

    private ComboBoxViewerCellEditor cellEditor;

    public MappingEditingSupport(ColumnViewer viewer) {
        super(viewer);
        setCellEditor(new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY));
        getCellEditor().setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof Resource) {
                    Resource resource = (Resource) element;
                    return resource.getUri();
                } else if (element instanceof Action) {
                    Action action = (Action) element;
                    String name = action.getType().name();
                    String actionName = name.substring(0, 1) + name.substring(1, name.length()).toLowerCase();
                    return actionName;
                } else if (element instanceof Flow) {
                    Flow flow = (Flow) element;
                    return flow.getName();
                }
                return super.getText(element);
            }
        });
        getCellEditor().setContentProvider(new ArrayContentProvider());
    }

  

    public void setCellEditor(ComboBoxViewerCellEditor cellEditor) {
        this.cellEditor = cellEditor;
    }
    
    public ComboBoxViewerCellEditor getCellEditor() {
        return cellEditor;
    }
    
    @Override
    protected boolean canEdit(Object element) {
        return true;
    }
    
    @Override
    protected CellEditor getCellEditor(Object element) {
        return cellEditor;
    }
}
