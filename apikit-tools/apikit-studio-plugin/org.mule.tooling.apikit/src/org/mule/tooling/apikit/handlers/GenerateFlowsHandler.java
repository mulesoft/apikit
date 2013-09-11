package org.mule.tooling.apikit.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.mule.tooling.apikit.Activator;
import org.mule.tooling.core.MuleCorePlugin;
import org.mule.tooling.core.model.IMuleProject;
import org.mule.tooling.messageflow.editor.MultiPageMessageFlowEditor;
import org.mule.tooling.messageflow.util.MessageFlowUtils;
import org.mule.tooling.ui.utils.SaveModifiedResourcesDialog;
import org.mule.tooling.ui.utils.UiUtils;
import org.mule.tools.apikit.ScaffolderAPI;
import org.raml.model.Raml;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.YamlDocumentValidator;
import org.raml.parser.visitor.YamlValidationService;


public class GenerateFlowsHandler extends AbstractHandler implements IHandler {
    
    private IWorkbenchWindow workbenchWindow;
    private File apiFolder;

    @Override
    public boolean isEnabled() {
        Activator activator = Activator.getDefault();
        IWorkbench workbench = activator.getWorkbench();
        workbenchWindow = workbench.getActiveWorkbenchWindow();
        ISelectionService selectionService = workbenchWindow
        .getSelectionService();
        IStructuredSelection structured = (IStructuredSelection) selectionService.getSelection();
     
        if (structured.getFirstElement() instanceof IFile) {
            // get the selected file
            IFile file = (IFile) structured.getFirstElement();
            // get the path
            IPath path = file.getLocation();
            
            YamlDocumentValidator ramlValidator = new YamlDocumentValidator(Raml.class);
            File ramlFile = file.getRawLocation().toFile();
            YamlValidationService validationService = new YamlValidationService(new CompositeResourceLoader(new DefaultResourceLoader(), new FileResourceLoader(ramlFile)), ramlValidator);
            String content;
            try {
                content = new Scanner(ramlFile).useDelimiter("\\Z").next();
                System.out.println(content);
                List<ValidationResult> validation = validationService.validate(content);
                System.out.println(path.toPortableString());
                if (ramlFile.getName().toLowerCase().endsWith("yaml") && validation.isEmpty()) {
                    apiFolder = ramlFile.getParentFile();
                    return true;
                } 
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } 
        }
        return false;
    }
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IMuleProject muleProject = MuleCorePlugin.getDesignContext().getMuleProject();
        IProject project = muleProject.getJavaProject().getProject();
        if (!saveModifiedResources(project)) {
            MessageDialog.openError(workbenchWindow.getShell(), "Generate flows", "The generation of flows failed. There are unsaved resources in the project.");
            return false;
        }
        IFolder appFolder = muleProject.getApplicationArtifactsFolder();
        ScaffolderAPI scaffolderAPI = new ScaffolderAPI(apiFolder, appFolder.getRawLocation().toFile());
        scaffolderAPI.run();
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
            MultiPageMessageFlowEditor multiPageFlowEditor = MessageFlowUtils.getInstance().getMultiPageMessageFlowEditor();
            multiPageFlowEditor.updateFlowFromSource();
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private boolean saveModifiedResources(IProject project) {
        List<IEditorPart> dirtyEditors = UiUtils.getDirtyEditors(project);
        if (dirtyEditors.isEmpty())
            return true;
        Shell shell = workbenchWindow.getShell();
        SaveModifiedResourcesDialog dialog = new SaveModifiedResourcesDialog(shell);
        if (dialog.open(shell, dirtyEditors))
            return true;
        return false;
    }
}
    