package org.mule.tools.apikit.model;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class API {
    private String baseUri;
    private APIKitConfig config;
    private File xmlFile;
    private File yamlFile;
    private static final Map<File, API> factory = new HashMap<File, API>();

    public static API createAPIBinding(File yamlFile, File xmlFile, String path) {
        return createAPIBinding(yamlFile, xmlFile, path, null);
    }

    public static API createAPIBinding(File yamlFile, File xmlFile, String path, APIKitConfig config) {
        Validate.notNull(yamlFile);

        if (factory.containsKey(yamlFile)) {
            API api = factory.get(yamlFile);

            if (api.xmlFile == null && xmlFile != null) {
                api.xmlFile = xmlFile;
            }

            return api;
        }

        API api = new API();

        api.yamlFile = yamlFile;
        api.xmlFile = xmlFile;
        api.baseUri = path;
        api.config = config;

        factory.put(yamlFile, api);

        return api;
    }

    private API() {
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
