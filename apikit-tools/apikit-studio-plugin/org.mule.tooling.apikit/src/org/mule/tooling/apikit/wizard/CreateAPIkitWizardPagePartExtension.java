package org.mule.tooling.apikit.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.mule.tooling.apikit.util.APIKitProjectHelper;
import org.mule.tooling.core.io.IMuleResources;
import org.mule.tooling.core.io.MuleResourceUtils;
import org.mule.tooling.core.model.IMuleProject;
import org.mule.tooling.model.messageflow.MuleConfiguration;
import org.mule.tooling.ui.newprojectwizard.IConfigurationTemplateExtension;
import org.mule.tooling.ui.wizards.extensible.BaseWizardPagePartExtension;
import org.mule.tooling.ui.wizards.extensible.WizardContext;

public class CreateAPIkitWizardPagePartExtension extends BaseWizardPagePartExtension implements IConfigurationTemplateExtension {

    private static final String APIKIT_SETTINGS = "APIKit Settings";

    private static final String ADD_APIKIT_COMPONENTS = "Add APIKit components";

    @WizardContext
    private String projectName;

    @WizardContext
    private IMuleProject muleProject;

    private boolean createAPIKit;

    public CreateAPIkitWizardPagePartExtension() {

    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setMuleProject(IMuleProject muleProject) {
        this.muleProject = muleProject;
    }

    @Override
    public List<String> getWizardPageIds() {
        return Arrays.asList("org.mule.tooling.ui.newmuleproject.third");
    }

    @Override
    public void createControl(Composite parent) {
        Group apikitGroupBox = new Group(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        layout.numColumns = 1;

        apikitGroupBox.setLayout(layout);
        apikitGroupBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        apikitGroupBox.setText(APIKIT_SETTINGS);
        final Button checkAPIkitButton = new Button(apikitGroupBox, SWT.CHECK);
        checkAPIkitButton.setText(ADD_APIKIT_COMPONENTS);
        checkAPIkitButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (checkAPIkitButton.getSelection()) {
                    setCreateAPIKit(true);
                } else {
                    setCreateAPIKit(false);
                }
            }
        });
        setPartComplete(true);
        parent.layout();
    }

    @Override
    public void performFinish(IProgressMonitor monitor) {
        APIKitProjectHelper projectHelper = new APIKitProjectHelper(muleProject);
        if (createAPIKit) {
            projectHelper.setAPIKitProjectEnabled(true);
        } else {
            projectHelper.setAPIKitProjectEnabled(false);
        }
    }

    public void setCreateAPIKit(boolean createAPIKit) {
        this.createAPIKit = createAPIKit;
    }

    @Override
    public boolean canExecute() {
        return createAPIKit;
    }

    @Override
    public IFile createConfiguration(IMuleProject muleProject, String filename, String name, String description) throws CoreException {
        URL resourceURL = NewAPIKitProjectWizard.class.getClassLoader().getResource("resources/sample");
        File rootTemplateProjectFolder;
        try {
            rootTemplateProjectFolder = new File(FileLocator.resolve(resourceURL).toURI());
            File appFolder = new File(rootTemplateProjectFolder, IMuleResources.MULE_MESSAGE_FLOWS_FOLDER);

            IFolder mflows = muleProject.getMessageFlowsFolder();
            IFile mflowFile = mflows.getFile(filename);

            File[] listFiles = appFolder.listFiles();
            FileInputStream muleConfigInputStream = new FileInputStream(listFiles[0]);
            MuleConfiguration muleConfig = MuleResourceUtils.loadMuleConfiguration(muleConfigInputStream);
            
            MuleResourceUtils.create(muleConfig, mflowFile);
            
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
            IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
            IDE.openEditor(page, mflowFile, true);
            BasicNewResourceWizard.selectAndReveal(mflowFile, activeWorkbenchWindow);

            return mflowFile;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
