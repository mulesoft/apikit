package org.mule.tooling.apikit.util;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.raml.model.Raml;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;

public enum APIKitHelper {

    INSTANCE;

    public boolean isBuildableYaml(String fileName, String content, ResourceLoader resourceLoader) {
        try {
            RamlDocumentBuilder builder = new RamlDocumentBuilder(resourceLoader);
            builder.build(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidYaml(String fileName, String content, ResourceLoader resourceLoader) {
        List<ValidationResult> validationResults = RamlValidationService.createDefault(resourceLoader).validate(content);
        if (validationResults != null && !validationResults.isEmpty()) {
            return false;
        }
        return true;
    }

    public Raml retrieveRaml(String fileName, InputStream content, ResourceLoader resourceLoader) {
        try {
            RamlDocumentBuilder builder = new RamlDocumentBuilder(resourceLoader);
            return builder.build(content);
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isRamlFile(File ramlFile) {
        String fileName = ramlFile.getName().toLowerCase();
        return (fileName.endsWith("yaml") || fileName.endsWith("yml") || fileName.endsWith("raml"));
    }
}
