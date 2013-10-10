/**
 * 
 */
package org.mule.tooling.apikit.widgets;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.mule.tooling.apikit.Activator;
import org.mule.tooling.apikit.action.AddMappingAction;
import org.mule.tooling.apikit.action.RemoveMappingAction;
import org.mule.tooling.apikit.util.APIKitHelper;
import org.mule.tooling.core.MuleConfigurationsCache;
import org.mule.tooling.core.MuleCorePlugin;
import org.mule.tooling.core.model.IMuleProject;
import org.mule.tooling.model.messageflow.Flow;
import org.mule.tooling.model.messageflow.MessageFlowNode;
import org.mule.tooling.model.messageflow.MuleConfiguration;
import org.mule.tooling.model.messageflow.decorator.PropertiesMap;
import org.mule.tooling.model.messageflow.decorator.PropertyCollectionMap;
import org.mule.tooling.ui.modules.core.widgets.AttributesPropertyPage;
import org.mule.tooling.ui.modules.core.widgets.IFieldEditor;
import org.mule.tooling.ui.modules.core.widgets.editors.CustomEditor;
import org.mule.tooling.ui.modules.core.widgets.editors.StringFieldEditor;
import org.mule.tooling.ui.modules.core.widgets.meta.AttributeHelper;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.FileResourceLoader;

public class APIKitMappingCustomEditor extends CustomEditor {

    private static final String RAML_EDITOR_NAME = "raml";
    private ToolBarManager manager;
    private TableViewer mappingsTableViewer;
    private IAction addMappingAction;
    private IAction removeMappingAction;
    private Collection<Mapping> mappings;
    private String ramlPath;
    private AttributesPropertyPage globalElementPage;

    public APIKitMappingCustomEditor(AttributesPropertyPage parentPage, AttributeHelper helper) {
        super(parentPage, helper);
        this.mappings = new ArrayList<Mapping>();
    }

    @Override
    protected Control createControl(AttributesPropertyPage parentPage) {
        globalElementPage = parentPage;

        Group parentComposite = getGroup(globalElementPage);
        GridLayoutFactory.swtDefaults().numColumns(1).equalWidth(false).applyTo(parentComposite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(parentComposite);

        Composite bar = new Composite(parentComposite, SWT.None);
        bar.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        createToolBar(bar);

        StringFieldEditor ramlEditor = getRamlEditor();
        final Text text = (Text) ramlEditor.getText();
        setRamlPath(text.getText());
        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                setRamlPath(text.getText());
            }
        });

        TableLayout tableLayout = new TableLayout();
        tableLayout.addColumnData(new ColumnWeightData(1));
        tableLayout.addColumnData(new ColumnWeightData(1));
        tableLayout.addColumnData(new ColumnWeightData(1));
        Table exampleTable = new Table(parentComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
        exampleTable.setLinesVisible(true);
        exampleTable.setHeaderVisible(true);
        exampleTable.setLayout(tableLayout);

        mappingsTableViewer = new TableViewer(exampleTable);
        mappingsTableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        TableViewerColumn resourceColumn = new TableViewerColumn(mappingsTableViewer, SWT.NONE);
        resourceColumn.getColumn().setText("Resource");
        final MappingEditingSupport resourceEditingSupport = new ResourceEditingSupport(resourceColumn.getViewer());
        resourceColumn.setEditingSupport(resourceEditingSupport);
        // resourceEditingSupport.getCellEditor().setInput(MappingManager.INSTANCE.getResources());

        TableViewerColumn actionColumn = new TableViewerColumn(mappingsTableViewer, SWT.NONE);
        actionColumn.getColumn().setText("Action");
        final MappingEditingSupport actionEditingSupport = new ActionEditingSupport(actionColumn.getViewer());
        actionColumn.setEditingSupport(actionEditingSupport);
        // actionEditingSupport.getCellEditor().setInput(MappingManager.INSTANCE.getResources());

        TableViewerColumn flowColumn = new TableViewerColumn(mappingsTableViewer, SWT.NONE);
        flowColumn.getColumn().setText("Flow");
        final MappingEditingSupport flowNameEditingSupport = new FlowNameEditingSupport(flowColumn.getViewer());
        flowColumn.setEditingSupport(flowNameEditingSupport);
        // flowNameEditingSupport.getCellEditor().setInput(MappingManager.INSTANCE.getFlowNames());

        mappingsTableViewer.setContentProvider(new ArrayContentProvider());
        mappingsTableViewer.setLabelProvider(new MappingsLabelProvider());

        mappingsTableViewer.getColumnViewerEditor().addEditorActivationListener(new ColumnViewerEditorActivationListener() {

            @Override
            public void beforeEditorActivated(ColumnViewerEditorActivationEvent event) {
                MappingAccesor mappingAccessor = new MappingAccesor();
                ViewerCell viewerCell = (ViewerCell) event.getSource();
                int columnIndex = viewerCell.getColumnIndex();
                switch (columnIndex) {
                case 0:
                    resourceEditingSupport.getCellEditor().setInput(mappingAccessor.generateResources());
                    break;
                case 1:
                    ViewerRow viewerRow = viewerCell.getViewerRow();
                    ViewerCell resourceCell = viewerRow.getCell(0);
                    Mapping mapping = (Mapping) resourceCell.getElement();
                    Resource resource = mapping.getResource();
                    actionEditingSupport.getCellEditor().setInput(resource.getActions().values());
                    break;
                default:
                    flowNameEditingSupport.getCellEditor().setInput(mappingAccessor.generateFlows());
                }
            }

            @Override
            public void afterEditorActivated(ColumnViewerEditorActivationEvent event) {

            }

            @Override
            public void beforeEditorDeactivated(ColumnViewerEditorDeactivationEvent event) {

            }

            @Override
            public void afterEditorDeactivated(ColumnViewerEditorDeactivationEvent event) {

            }
        });

        mappingsTableViewer.setInput(mappings);
        mappingsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                getRemoveMappingAction().setEnabled(getRemoveMappingAction().isEnabled());
                manager.update(true);
            }
        });

        return parentComposite;
    }

    private Raml retrieveRamlSpec() throws Exception {
        File ramlFile = retrieveRamlFile();
        if (ramlFile != null && ramlFile.exists()) {
            InputStream inputStream = new FileInputStream(ramlFile);
            CompositeResourceLoader resourceLoader = new CompositeResourceLoader(new DefaultResourceLoader(), new FileResourceLoader(ramlFile.getParentFile()));
            return APIKitHelper.INSTANCE.retrieveRaml(ramlFile.getName(), inputStream, resourceLoader);
        }
        return null;
    }

    private Group getGroup(final AttributesPropertyPage parentPage) {
        Group parent = null;
        for (Control control : parentPage.getChildren()) {
            if (control instanceof Group) {
                Group group = (Group) control;
                if (group.getText().equalsIgnoreCase("mappings")) {
                    parent = group;
                    break;
                }
            }
        }

        if (parent == null) {
            parent = new Group(parentPage, SWT.NONE);
            parent.setText("Mappings");
        }
        return parent;
    }

    private void createToolBar(Composite bar) {
        final ToolBar toolBar = new ToolBar(bar, SWT.NONE);
        manager = new ToolBarManager(toolBar);
        MappingAccesor mappingAccessor = new MappingAccesor();
        addCommonActions(manager, mappingAccessor);
        manager.update(true);
    }

    private void addCommonActions(ToolBarManager manager, MappingAccesor mappingAccessor) {
        setAddMappingAction(new AddMappingAction(mappingAccessor));
        setRemoveMappingAction(new RemoveMappingAction(mappingAccessor));
        manager.add(getAddMappingAction());
        manager.add(getRemoveMappingAction());
    }

    @Override
    public void loadFrom(MessageFlowNode node, PropertyCollectionMap props) {
        for (PropertyCollectionMap propertyCollectionMap : props.getPropertyCollections().values()) {
            PropertiesMap propertiesMap = propertyCollectionMap.getPropertiesMap();
            String resource = propertiesMap.getProperty("resource", "");
            String action = propertiesMap.getProperty("action", "");
            String flow = propertiesMap.getProperty("flow-ref", "");
            addMapping(resource, action, flow);
        }
        mappingsTableViewer.setInput(mappings);
    }

    private void addMapping(String resourceUri, String actionName, String flowName) {
        Resource resource = new Resource();
        resource.setRelativeUri(resourceUri);
        resource.setParentUri("");
        Action action = new Action();
        action.setType(ActionType.valueOf(actionName.toUpperCase()));
        Flow flow = new Flow();
        flow.setName(flowName);
        mappings.add(new Mapping(resource, action, flow));
    }

    @Override
    public void saveTo(MessageFlowNode node, PropertyCollectionMap props) {
        props.getPropertyCollections().clear();
        int i = 0;
        for (Mapping mapping : mappings) {
            PropertyCollectionMap subMap = new PropertyCollectionMap();
            subMap.addProperty("flow-ref", mapping.getFlow().getName());
            subMap.addProperty("resource", mapping.getResource().getUri());
            subMap.addProperty("action", mapping.getAction().getType().name().toLowerCase());
            props.addPropertyCollection("@http://www.mulesoft.org/schema/mule/apikit/flow-mapping;" + i, subMap);
            i++;
        }
    }

    public IAction getAddMappingAction() {
        return addMappingAction;
    }

    public void setAddMappingAction(IAction addMappingAction) {
        this.addMappingAction = addMappingAction;
    }

    public IAction getRemoveMappingAction() {
        return removeMappingAction;
    }

    public void setRemoveMappingAction(IAction removeMappingAction) {
        this.removeMappingAction = removeMappingAction;
    }

    public class MappingAccesor {

        public TableViewer getViewer() {
            return mappingsTableViewer;
        }

        public Mapping getSelectedMapping() {
            if (getViewer() != null) {
                IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
                if (selection != null) {
                    Mapping firstElement = (Mapping) selection.getFirstElement();
                    if (firstElement != null) {
                        return firstElement;
                    }
                }
            }
            return null;
        }

        public void addMapping(Mapping mapping) {
            mappings.add(mapping);
            mappingsTableViewer.setInput(mappings);
        }

        public void removeMapping(Mapping mapping) {
            mappings.remove(mapping);
            mappingsTableViewer.setInput(mappings);
        }

        public Raml retrieveRamlSpec() throws Exception {
            return APIKitMappingCustomEditor.this.retrieveRamlSpec();
        }

        public File retrieveRamlFile() {
            return APIKitMappingCustomEditor.this.retrieveRamlFile();
        }

        public Collection<Resource> generateResources() {
            Raml ramlSpec;
            try {
                ramlSpec = retrieveRamlSpec();
                if (ramlSpec != null) {
                    return MappingManager.INSTANCE.retrieveResources(ramlSpec);
                }
                return Collections.emptyList();
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }

        public Collection<Flow> generateFlows() {
            IMuleProject muleProject = MuleCorePlugin.getDesignContext().getMuleProject();
            List<Flow> flows = new ArrayList<Flow>();
            for (MuleConfiguration muleConfiguration : MuleConfigurationsCache.getDefaultInstance().getConfigurations(muleProject)) {
                flows.addAll(muleConfiguration.getFlows());
            }
            return flows;
        }
    }

    private StringFieldEditor getRamlEditor() {
        final IFieldEditor ramlFileEditor = globalElementPage.getEditors().get(RAML_EDITOR_NAME);
        if (ramlFileEditor != null && ramlFileEditor instanceof StringFieldEditor) {
            final StringFieldEditor ramlRefEditor = (StringFieldEditor) ramlFileEditor;
            return ramlRefEditor;
        }
        return null;
    }

    private File retrieveRamlFile() {
        File ramlFile = null;
        IMuleProject muleProject = MuleCorePlugin.getDesignContext().getMuleProject();
        if (getRamlPath() != null && !getRamlPath().isEmpty()) {
            ramlFile = muleProject.getFile(getRamlPath()).getRawLocation().toFile();
            if (ramlFile != null && ramlFile.exists()) {
                return ramlFile;
            } else {
                ramlFile = muleProject.getFile(Activator.API_FOLDER + File.separator + getRamlPath()).getRawLocation().toFile();
                if (ramlFile != null && ramlFile.exists()) {
                    return ramlFile;
                }
            }
            return ramlFile;
        }
        return null;
    }

    public String getRamlPath() {
        return ramlPath;
    }

    public void setRamlPath(String ramlPath) {
        this.ramlPath = ramlPath;
    }

    @Override
    public void refreshOptions() {

    }

}
