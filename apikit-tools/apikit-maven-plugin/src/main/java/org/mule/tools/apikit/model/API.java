package org.mule.tools.apikit.model;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class API {

    public static final int DEFAULT_PORT = 8081;
    public static final String DEFAULT_BASE_URI = "http://localhost:" + DEFAULT_PORT + "/api";

    private String baseUri;
    private APIKitConfig config;
    private File xmlFile;
    private File yamlFile;

    public API(File yamlFile, File xmlFile, String baseUri) {
        this.baseUri = baseUri;
        this.yamlFile = yamlFile;
        this.xmlFile = xmlFile;
    }

    public API(File yamlFile, File xmlFile, String baseUri, APIKitConfig config) {
        this(yamlFile, xmlFile, baseUri);
        this.config = config;
    }

    public File getXmlFile() {
        return xmlFile;
    }

    public void setXmlFile(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    public File getXmlFile(File rootDirectory) {
        // Case we need to create the file
        if (xmlFile == null) {
            xmlFile = new File(rootDirectory,
                    FilenameUtils.getBaseName(
                            yamlFile.getAbsolutePath()) + ".xml");
        }
        return xmlFile;
    }

    public File getYamlFile() {
        return yamlFile;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public APIKitConfig getConfig() {
        return config;
    }

    public void setConfig(APIKitConfig config) {
        this.config = config;
    }

    public void setDefaultConfig() {
        config = new APIKitConfig.Builder(yamlFile.getName()).setName(APIKitConfig.DEFAULT_CONFIG_NAME).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        API api = (API) o;

        if (!yamlFile.equals(api.yamlFile)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return yamlFile.hashCode();
    }
}
