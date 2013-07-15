package org.mule.tools.apikit.input;

import org.apache.maven.plugin.logging.Log;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.output.GenerationModelProvider;
import org.mule.tools.apikit.model.ResourceActionPair;
import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.visitor.YamlDocumentBuilder;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class RAMLFilesParser {
    private final GenerationModelProvider generationModelProvider;
    private Set<ResourceActionPair> entries = new HashSet<ResourceActionPair>();

    public RAMLFilesParser(Log log, GenerationModelProvider generationModelProvider, Map<File, InputStream> fileStreams) {
        this.generationModelProvider = generationModelProvider;
        List<File> processedFiles = new ArrayList<File>();
        for (Map.Entry<File, InputStream> fileInputStreamEntry : fileStreams.entrySet()) {
            YamlDocumentBuilder<Raml> builderNodeHandler = new YamlDocumentBuilder<Raml>(Raml.class);
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
                API api = API.createAPIBinding(filename, null, baseUri, null);
                ResourceActionPair resourceActionPair = new ResourceActionPair(api, resource.getUri(),
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
