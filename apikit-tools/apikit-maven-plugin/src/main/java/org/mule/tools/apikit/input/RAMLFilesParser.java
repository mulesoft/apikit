package org.mule.tools.apikit.input;

import org.apache.maven.plugin.logging.Log;

import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.output.GenerationModelProvider;
import org.mule.tools.apikit.model.ResourceActionPair;
import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.visitor.YamlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class RAMLFilesParser {
    private final GenerationModelProvider generationModelProvider;
    private Set<ResourceActionPair> entries = new HashSet<ResourceActionPair>();

    public static final String API_HOME = "src/main/api";

    public RAMLFilesParser(Log log, GenerationModelProvider generationModelProvider, Map<File, InputStream> fileStreams) {
        this.generationModelProvider = generationModelProvider;
        List<File> processedFiles = new ArrayList<File>();
        for (Map.Entry<File, InputStream> fileInputStreamEntry : fileStreams.entrySet()) {
            YamlDocumentBuilder<Raml> builderNodeHandler = new YamlDocumentBuilder<Raml>(Raml.class, new CompositeResourceLoader(new ResourceLoader()
            {
                @Override
                public InputStream fetchResource(String resource)
                {
                    File file = new File(API_HOME, resource);
                    if(file.exists()) {
                        try {
                            return new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            // Do nothing
                        }
                    }
                    return null;
                }
            }, new ResourceLoader()
            {
                @Override
                public InputStream fetchResource(String resource)
                {
                    File file = new File(resource);
                    if(file.exists()) {
                        try {
                            return new FileInputStream(file);
                        } catch(FileNotFoundException e) {
                            // Do nothing
                        }
                    }
                    return null;
                }

            }, new DefaultResourceLoader()));
            try {
                Raml raml = builderNodeHandler.build(fileInputStreamEntry.getValue());
                collectResources(fileInputStreamEntry.getKey(), entries, raml.getResources(), raml.getBaseUri());
                processedFiles.add(fileInputStreamEntry.getKey());
            } catch (Exception e) {
                log.debug("Could not parse [" + fileInputStreamEntry.getKey() + "] as root yaml file.");
                log.debug(e);
            }

        }
        if (processedFiles.size() > 0) {
            log.info("The following YAML files were parsed correctly: " +
                    processedFiles);
        } else {
            log.error("YAML Root not found. None of the files were recognized as root yaml files.");
        }
    }

    void collectResources(File filename, Set<ResourceActionPair> resources, Map<String, Resource> resourceMap, String baseUri) {
        for (Resource resource : resourceMap.values()) {
            for (Action action : resource.getActions().values()) {
                API api = API.createAPIBinding(filename, null, baseUri, new APIKitConfig.Builder(filename.getName()).build());
                String path = APIKitTools.getPathFromUri(baseUri);
                ResourceActionPair resourceActionPair = new ResourceActionPair(api, path + resource.getUri(),
                        action.getType().toString());
                generationModelProvider.add(api, resourceActionPair, resource, action);
                resources.add(resourceActionPair);
            }

            collectResources(filename, resources, resource.getResources(), baseUri);
        }
    }

    public Set<ResourceActionPair> getEntries() {
        return entries;
    }
}
