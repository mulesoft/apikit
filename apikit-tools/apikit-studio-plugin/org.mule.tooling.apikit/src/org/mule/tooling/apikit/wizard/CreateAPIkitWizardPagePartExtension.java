package org.mule.tooling.apikit.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.mule.tooling.apikit.Activator;
import org.mule.tooling.apikit.scaffolder.FlowGenerator;
import org.mule.tooling.apikit.util.APIKitHelper;
import org.mule.tooling.apikit.util.APIKitProjectHelper;
import org.mule.tooling.core.MuleCorePlugin;
import org.mule.tooling.core.io.IMuleResources;
import org.mule.tooling.core.io.MuleResourceUtils;
import org.mule.tooling.core.model.IMuleProject;
import org.mule.tooling.model.messageflow.MuleConfiguration;
import org.mule.tooling.ui.common.FileChooserComposite;
import org.mule.tooling.ui.newprojectwizard.IConfigurationTemplateExtension;
import org.mule.tooling.ui.wizards.extensible.BaseWizardPagePartExtension;
import org.mule.tooling.ui.wizards.extensible.WizardContext;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.FileResourceLoader;

public class CreateAPIkitWizardPagePartExtension extends BaseWizardPagePartExtension implements IConfigurationTemplateExtension {

    private static final String RAML_FILE = "RAML file:";

	private static final String INVALID_YAML_FILE = "The yaml file does not exist or is not a valid RAML file";

    private static final String APIKIT_SETTINGS = "APIkit Settings";

    private static final String ADD_APIKIT_COMPONENTS = "Add APIkit components";

    @WizardContext
    private String projectName;

    @WizardContext
    private IMuleProject muleProject;

    private boolean createAPIKit;

    private FileChooserComposite fileChooser;
    
    private File ramlFile;

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
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;

        apikitGroupBox.setLayout(layout);
        apikitGroupBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        apikitGroupBox.setText(APIKIT_SETTINGS);
        final Button checkAPIkitButton = new Button(apikitGroupBox, SWT.CHECK);
        checkAPIkitButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
        checkAPIkitButton.setText(ADD_APIKIT_COMPONENTS);
        checkAPIkitButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (checkAPIkitButton.getSelection()) {
                    setCreateAPIKit(true);
                    fileChooser.setEnabled(true);
                } else {
                    setCreateAPIKit(false);
                    fileChooser.setEnabled(false);
                }
            }
        });
        
        final Label ramlFileName = new Label(apikitGroupBox, SWT.NONE);
        ramlFileName.setText(RAML_FILE);
        ramlFileName.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));

        fileChooser = new FileChooserComposite(apikitGroupBox, SWT.NULL);
        fileChooser.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
        fileChooser.addSelectionListener(new ISelectionListener() {
            @Override
            public void selectionChanged(IWorkbenchPart part, ISelection selection) {
                try {
                    updateRAMLFile();
                } catch (FileNotFoundException e) {
                    ramlFile = null;
                }
                updateErrorMessage();
                updatePagePartComplete();
            }

        });
        fileChooser.setEnabled(false);
        setPartComplete(true);
        parent.layout();
    }
    
    private void updatePagePartComplete() {
        String errorDescription = this.retrieveErrorMessage();
        if (errorDescription.isEmpty()) {
            setPartComplete(true);
        } else {
            setPartComplete(false);
        }
    }
    
    private void updateErrorMessage() {
        String errorDescription = this.retrieveErrorMessage();
        if (errorDescription.isEmpty()) {
            this.notifyErrorsCleared();
        } else {
            this.notifyErrorInConfiguration(errorDescription);
        }
    }

    private String retrieveErrorMessage() {
        if (ramlFile == null && !fileChooser.getFilePath().isEmpty()) {
            return INVALID_YAML_FILE;
        }
        return "";
    }
    
    private void updateRAMLFile() throws FileNotFoundException {
        String filePath = fileChooser.getFilePath();
        File tempFile = new File(filePath);
        if (tempFile.exists() && APIKitHelper.INSTANCE.isRamlFile(tempFile)) {
            String content = new Scanner(tempFile).useDelimiter("\\Z").next();
            CompositeResourceLoader resourceLoader = new CompositeResourceLoader(new DefaultResourceLoader(), new FileResourceLoader(tempFile.getParentFile()));
            if (APIKitHelper.INSTANCE.isValidYaml(content, resourceLoader)) {
                ramlFile = tempFile;
                return;
            }
        }
        ramlFile = null;
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
    	IFolder mflows = muleProject.getMessageFlowsFolder();
    	if (ramlFile != null) {
    		//creates the Mule configuration from a RAML file -> run the scaffolder
    		try {
				IFolder apiFolder = muleProject.getFolder(Activator.API_FOLDER);
				FileUtils.copyFileToDirectory(ramlFile, apiFolder.getRawLocation().toFile());
				//delete the default api.yaml file
				File defaultRAMLFile = apiFolder.getFile(Activator.DEFAULT_RAML_FILE).getRawLocation().toFile();
				if (defaultRAMLFile.exists()) {
					FileUtils.forceDelete(defaultRAMLFile);
				}
				FlowGenerator flowGenerator = new FlowGenerator();
				IFile ramlFileInProject = apiFolder.getFile(ramlFile.getName());
	            flowGenerator.run(new NullProgressMonitor(), muleProject.getJavaProject().getProject(), ramlFileInProject);
	            flowGenerator.createMuleConfigs(new NullProgressMonitor(), muleProject);
	            String mFlowName = FilenameUtils.removeExtension(ramlFileInProject.getName());
	            IFile mflowFile = mflows.getFile(mFlowName + "." + IMuleResources.MULE_MESSAGE_FLOW_SUFFIX);
	            return mflowFile;
			} catch (IOException e) {
				MuleCorePlugin.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				return createConfigWithAPIkitComponents(filename, mflows);
			}
    	} else {
    		return createConfigWithAPIkitComponents(filename, mflows);
    	}
    }

	private IFile createConfigWithAPIkitComponents(String filename,
			IFolder mflows) throws CoreException, PartInitException {
		URL resourceURL = CreateAPIkitWizardPagePartExtension.class.getClassLoader().getResource(Activator.EXAMPLE_PROJECT_ROOT);
		File rootTemplateProjectFolder;
		try {
			rootTemplateProjectFolder = new File(FileLocator.resolve(resourceURL).toURI());
			File appFolder = new File(rootTemplateProjectFolder, IMuleResources.MULE_MESSAGE_FLOWS_FOLDER);
			
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
			MuleCorePlugin.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		} catch (IOException e) {
			MuleCorePlugin.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		return null;
	}

}
