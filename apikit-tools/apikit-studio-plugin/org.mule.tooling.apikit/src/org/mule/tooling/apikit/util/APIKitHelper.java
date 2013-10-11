package org.mule.tooling.apikit.util;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.core.resources.IFile;
import org.raml.editor.util.ProjectClasspathRunner;
import org.raml.model.Raml;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;

public enum APIKitHelper {

    INSTANCE;

    public boolean isBuildableYaml(IFile file, final String content, final ResourceLoader resourceLoader) {
        try {
            new ProjectClasspathRunner().run(new Callable<Raml>() {

                @Override
                public Raml call() throws Exception {
                    RamlDocumentBuilder builder = new RamlDocumentBuilder(resourceLoader);
                    return builder.build(content);
                }
            }, file.getProject());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Raml retrieveRaml(IFile ramlFile, final InputStream content, final ResourceLoader resourceLoader) {
        Raml raml = new ProjectClasspathRunner().run(new Callable<Raml>() {
            @Override
            public Raml call() throws Exception {
                RamlDocumentBuilder builder = new RamlDocumentBuilder(resourceLoader);
                return builder.build(content);
            }
        }, ramlFile.getProject());
        return raml;
    }

    public boolean isRamlFile(File ramlFile) {
        String fileName = ramlFile.getName().toLowerCase();
        return (fileName.endsWith("yaml") || fileName.endsWith("yml") || fileName.endsWith("raml"));
    }

    public boolean isValidYaml(IFile file, final String content, final CompositeResourceLoader resourceLoader) {
        List<ValidationResult> validationResults = new ProjectClasspathRunner().run(new Callable<List<ValidationResult>>() {

            @Override
            public List<ValidationResult> call() throws Exception {
                return RamlValidationService.createDefault(resourceLoader).validate(content);
            }
        }, file.getProject());

        if (validationResults != null && !validationResults.isEmpty()) {
            return false;
        }
        return true;
    }
}
