/**
 * 
 */
package org.mule.tooling.apikit.deps;

import java.util.concurrent.Callable;

import org.eclipse.core.resources.IProject;
import org.raml.editor.util.ProjectClasspathRunner;


/**
 * @author Sebastian Sampaoli
 *
 */
public class APIkitProjectClasspathRunner {
    
    private ProjectClasspathRunner runner;
    
    public APIkitProjectClasspathRunner() {
        runner = new ProjectClasspathRunner();
    }
    
    public <T> T run(Callable<T> callable, IProject project) {
        return runner.run(callable, project);
    }
    
}
