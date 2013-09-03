package org.mule.tooling.apikit.wizard;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.mule.tooling.core.MuleCorePlugin;
import org.mule.tooling.core.runtime.server.ServerDefinition;
import org.mule.tooling.ui.jface.MuleLabelProvider;

public class NewAPIKitProjectWizardPage extends WizardPage {
	
	private static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";
	private static final String DEFAULT_ARTIFACT_ID = "hello-APIKit";
	private static final String DEFAULT_GROUP_ID = "org.mule.modules.hello";
	private Text groupId;
	private Text artifactId;
	private Text version;
    private Text projectName;
    private ComboViewer runtimeCombo;


	public NewAPIKitProjectWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("New APIKit project");
		setDescription("This wizard creates a new APIKit project");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.makeColumnsEqualWidth = false;
		layout.numColumns = 2;
		layout.verticalSpacing = 6;
		
		Label label = new Label(container, SWT.NULL);
        label.setText("&Project name:");
        
		projectName = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        projectName.setLayoutData(gd);
        projectName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });
        
        Group mavenSettingsGroup = new Group(container, SWT.NULL);
        GridDataFactory.fillDefaults().span(2, 1).grab(true, false).align(GridData.FILL, GridData.BEGINNING).applyTo(mavenSettingsGroup);
        layout = new GridLayout(2, false);
        mavenSettingsGroup.setLayout(layout);
        mavenSettingsGroup.setText("Maven Settings");
        
		label = new Label(mavenSettingsGroup, SWT.NULL);
		label.setText("&Group id:");

		groupId = new Text(mavenSettingsGroup, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		groupId.setLayoutData(gd);
		groupId.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});


		label = new Label(mavenSettingsGroup, SWT.NULL);
		label.setText("&Artifact id:");

		artifactId = new Text(mavenSettingsGroup, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		artifactId.setLayoutData(gd);
		artifactId.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		label = new Label(mavenSettingsGroup, SWT.NULL);
		label.setText("&Version:");

		version = new Text(mavenSettingsGroup, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		version.setLayoutData(gd);
		version.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initializeRuntimesCombo(container);
		initialize();
		dialogChanged();
		setControl(container);
	}


	private void initialize() {
		groupId.setText(DEFAULT_GROUP_ID);
		artifactId.setText(DEFAULT_ARTIFACT_ID);
		version.setText(DEFAULT_VERSION);
	}

	private void dialogChanged() {

		String version = getVersion();

		if (version.length() == 0) {
			updateStatus("Version must be specified");
			return;
		}
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getGroupId() {
		return groupId.getText();
	}

	public String getArtifactId() {
		return artifactId.getText();
	}

	public String getVersion() {
		return version.getText();
	}
	
	public String getProjectName() {
	    return projectName.getText();
	}
	
	private void initializeRuntimesCombo(Composite container) {
        // runtimes combo
        List<ServerDefinition> runtimes = MuleCorePlugin.getServerManager().getServerDefinitions();
        Label rtLabel = new Label(container, SWT.NULL);
        rtLabel.setText("Server Runtime:");
        rtLabel.setToolTipText("Choose a server runtime.");
        runtimeCombo = new ComboViewer(container, SWT.SIMPLE | SWT.READ_ONLY | SWT.DROP_DOWN);
        runtimeCombo.setContentProvider(new ArrayContentProvider());
        runtimeCombo.setLabelProvider(new MuleLabelProvider());
        runtimeCombo.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        runtimeCombo.setInput(runtimes);
        if (runtimes.size() > 0) {
            runtimeCombo.setSelection(new StructuredSelection(runtimes.get(0)));
        }
    }
	
	public/* just for test */void ensureNecessaryFoldersExists(File rootFile) {
        String[] pathsThatMustExist = { "flows", "mappings", "src/main/app", "src/main/java", "src/main/resources", "src/test/java", "src/test/resources" };

        for (String path : pathsThatMustExist) {
            File file = new File(rootFile, path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }
	
	/**
     * Get the id of the selected server runtime or null if none is selected.
     * 
     * @return
     */
    protected String getSelectedRuntimeId() {
        StructuredSelection selection = (StructuredSelection) runtimeCombo.getSelection();
        ServerDefinition selected = (ServerDefinition) selection.getFirstElement();
        return (selected != null) ? selected.getId() : null;
    }
}
