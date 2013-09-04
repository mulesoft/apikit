/**
 * 
 */
package org.mule.tooling.apikit.wizard;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.mule.tooling.apikit.template.MavenParameterReplacer;
import org.mule.tooling.apikit.template.TemplateFileWriter;
import org.mule.tooling.core.packageManager.ImportPackageManager;


/**
 * @author Sebastian Sampaoli
 *
 */
public class ImportAPIKitProjectManager extends ImportPackageManager {

    private PojoMavenModel mavenModel;
    public static final String POM_FILENAME = "pom.xml";
    public static final String POM_TEMPLATE_PATH = "/templates/pom.xml.tmpl";

    /**
     * @param mavenModel 
     * @param projectName
     * @param rootFile
     * @param runtimeId
     * @param copyToWorkspace
     */
    public ImportAPIKitProjectManager(PojoMavenModel mavenModel, String projectName, File rootFile, String runtimeId, boolean copyToWorkspace) {
        super(projectName, rootFile, runtimeId, copyToWorkspace);
        this.mavenModel = mavenModel;
    }
    
    /**
     * Initializes the project as a Java project.
     * 
     * @param projectName
     * @param monitor
     * @return
     * @throws CoreException
     * @throws IOException
     */
    protected IJavaProject initJavaProject(final String projectName, final File projectFile, IProjectDescription desc, final IProgressMonitor monitor) throws CoreException,
            IOException {
        IJavaProject javaProject = super.initJavaProject(projectName, projectFile, desc, monitor);
        generatePomFile(javaProject.getProject());
        return javaProject;
    }
    
    private void generatePomFile(IProject project) {
        TemplateFileWriter templateWriter = new TemplateFileWriter(project, new NullProgressMonitor());
        try {
            templateWriter.apply(POM_TEMPLATE_PATH, POM_FILENAME, new MavenParameterReplacer(mavenModel));
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
}
