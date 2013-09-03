/**
 * 
 */
package org.mule.tooling.apikit.wizard;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.mule.tooling.apikit.template.MavenParameterReplacer;
import org.mule.tooling.apikit.template.TemplateFileWriter;
import org.mule.tooling.core.builder.MuleNature;
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
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IProject project = root.getProject(projectName);

        // Project is created in the default location
        project.create(desc, new SubProgressMonitor(monitor, 1));
        copyEclipseDescriptorFiles(projectFile, monitor);
        copyMuleToolingConfigFile(projectFile);
        project.open(new SubProgressMonitor(monitor, 1));
        generatePomFile(project);

        // The location in the desription is null, so the default location is used
        final IProjectDescription description = project.getDescription();
        String[] natures = description.getNatureIds();
        String[] newNatures = new String[natures.length + 2];
        System.arraycopy(natures, 0, newNatures, 0, natures.length);

        newNatures[newNatures.length - 2] = JavaCore.NATURE_ID;
        newNatures[newNatures.length - 1] = MuleNature.NATURE_ID;

        description.setNatureIds(newNatures);
        project.setDescription(description, null);

        final IJavaProject javaProject = JavaCore.create(project);

        final Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
        entries.addAll(Arrays.asList(javaProject.getRawClasspath()));
        entries.add(JavaRuntime.getDefaultJREContainerEntry());
        javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), new SubProgressMonitor(monitor, 1));

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
