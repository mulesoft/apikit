package org.mule.tooling.apikit.util;

import java.io.InputStream;

import org.raml.model.Raml;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;


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
    
    public Raml retrieveRaml(String fileName, InputStream content, ResourceLoader resourceLoader) {
        try {
            RamlDocumentBuilder builder = new RamlDocumentBuilder(resourceLoader);
            return builder.build(content);
        } catch (Exception e) {
            return null;
        }
    }
}
