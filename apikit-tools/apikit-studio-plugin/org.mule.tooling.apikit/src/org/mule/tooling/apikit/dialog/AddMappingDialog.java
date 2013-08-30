/**
 * 
 */
package org.mule.tooling.apikit.dialog;

import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.mule.tooling.apikit.widgets.APIKitMappingCustomEditor.MappingAccesor;
import org.mule.tooling.apikit.widgets.Mapping;
import org.mule.tooling.apikit.widgets.MappingManager;
import org.mule.tooling.model.messageflow.Flow;
import org.raml.model.Action;
import org.raml.model.Resource;

/**
 * Dialog with fields for name and value to edit a metadata item.
 * 
 * @author Sebastian Sampaoli
 * 
 */
public class AddMappingDialog extends TitleAreaDialog {

    private ComboViewer actionViewer;
    private ComboViewer resourceViewer;
    private ComboViewer flowViewer;
    private Mapping generatedMapping;
    private MappingAccesor mappingAccessor;

    public AddMappingDialog(Shell parentShell, MappingAccesor mappingAccessor) {
        super(parentShell);
        this.mappingAccessor = mappingAccessor;
        this.generatedMapping = new Mapping();
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("New Mapping");
    }

    protected Control createDialogArea(Composite parent) {
        parent = (Composite) super.createDialogArea(parent);

        setTitle("Mapping");
        setMessage("Add a new mapping");

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().equalWidth(false).numColumns(2).applyTo(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(composite);
        
        ISelectionChangedListener comboListener = new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection.size() > 0) {
                    updateDialogComplete();
                }
            }
        };
        ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateDialogComplete();
            }
        };

        Label resourceLabel = new Label(composite, SWT.NONE);
        final GridDataFactory gridDataBeginning = GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(6, 6).span(1, 1).grab(false, false);
        gridDataBeginning.applyTo(resourceLabel);
        resourceLabel.setText("Resource:");

        resourceViewer = new ComboViewer(composite, SWT.NONE);
        resourceViewer.setContentProvider(new ArrayContentProvider());
        resourceViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof Resource) {
                    Resource resource = (Resource) element;
                    return resource.getUri();
                }
                return super.getText(element);
            }
        });
        resourceViewer.setInput(mappingAccessor.generateResources());
        
        resourceViewer.addSelectionChangedListener(comboListener);
        resourceViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) resourceViewer.getSelection();
                if (selection != null) {
                    Resource firstElement = (Resource) selection.getFirstElement();
                    Collection<Action> actions = firstElement.getActions().values();
                    actionViewer.setInput(actions);
                    generatedMapping.setResource(firstElement);
                } else {
                    generatedMapping.setResource(null);
                    actionViewer.setInput(MappingManager.INSTANCE.getAllActions());
                }
            }
        });
        resourceViewer.getCombo().addModifyListener(modifyListener);
        resourceViewer.getCombo().addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String resourceUri = resourceViewer.getCombo().getText();
                if (!resourceUri.isEmpty()) {
                    Resource resource = new Resource();
                    resource.setRelativeUri(resourceUri);
                    resource.setParentUri("");
                    generatedMapping.setResource(resource);
                } else {
                    generatedMapping.setResource(null);
                    actionViewer.setInput(MappingManager.INSTANCE.getAllActions());
                }
            }
        });
        final GridDataFactory gridData = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(6, 6);
        gridData.span(1, 1).grab(true, false).applyTo(resourceViewer.getControl());

        Label actionLabel = new Label(composite, SWT.NONE);
        gridDataBeginning.applyTo(actionLabel);
        actionLabel.setText("Action:");

        actionViewer = new ComboViewer(composite, SWT.READ_ONLY);
        actionViewer.setContentProvider(new ArrayContentProvider());
        actionViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof Action) {
                    Action action = (Action) element;
                    String name = action.getType().name();
                    String actionName = name.substring(0, 1) + name.substring(1, name.length()).toLowerCase();
                    return actionName;
                }
                return super.getText(element);
            }
        });
        actionViewer.setInput(MappingManager.INSTANCE.getAllActions());
        actionViewer.addSelectionChangedListener(comboListener);
        actionViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) actionViewer.getSelection();
                if (selection != null) {
                    Action firstElement = (Action) selection.getFirstElement();
                    generatedMapping.setAction(firstElement);
                } else {
                    generatedMapping.setAction(null);
                }
            }
        });
        gridData.span(1, 1).grab(true, false).applyTo(actionViewer.getControl());

        Label flowNameLabel = new Label(composite, SWT.NONE);
        gridDataBeginning.applyTo(flowNameLabel);
        flowNameLabel.setText("Flow:");

        flowViewer = new ComboViewer(composite, SWT.NONE);
        flowViewer.setContentProvider(new ArrayContentProvider());
        flowViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof Flow) {
                    Flow flow = (Flow) element;
                    return flow.getName();
                }
                return super.getText(element);
            }
        });
        flowViewer.setInput(mappingAccessor.generateFlows());
        flowViewer.addSelectionChangedListener(comboListener);
        flowViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) flowViewer.getSelection();
                if (selection != null) {
                    Flow firstElement = (Flow) selection.getFirstElement();
                    generatedMapping.setFlow(firstElement);
                } else {
                    generatedMapping.setFlow(null);
                }
            }
        });
        flowViewer.getCombo().addModifyListener(modifyListener);
        flowViewer.getCombo().addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String flowName = flowViewer.getCombo().getText();
                if (!flowName.isEmpty()) {
                    Flow flow = new Flow();
                    flow.setName(flowName);
                    generatedMapping.setFlow(flow);
                } else {
                    generatedMapping.setFlow(null);
                }
            }
        });
        gridData.span(1, 1).grab(true, false).applyTo(flowViewer.getControl());

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }
    
    private void updateDialogComplete() {
        boolean isComplete = resourceViewer.getCombo().getText().isEmpty() || actionViewer.getCombo().getText().isEmpty() || flowViewer.getCombo().getText().isEmpty();
        getButton(IDialogConstants.OK_ID).setEnabled(!isComplete);
    }

    public Mapping getGeneratedMapping() {
        return generatedMapping;
    }
    
    public void setGeneratedMapping(Mapping mapping) {
        this.generatedMapping = mapping;
    }
    
//    public Resource getResource() {
//        IStructuredSelection selection = (IStructuredSelection) resourceViewer.getSelection();
//        return (Resource) selection.getFirstElement();
//    }
//    
//    public Action getAction() {
//        IStructuredSelection selection = (IStructuredSelection) actionViewer.getSelection();
//        return (Action) selection.getFirstElement();
//    }
//    
//    public Flow getFlow() {
//        IStructuredSelection selection = (IStructuredSelection) flowViewer.getSelection();
//        return (Flow) selection.getFirstElement();
//    }
}
