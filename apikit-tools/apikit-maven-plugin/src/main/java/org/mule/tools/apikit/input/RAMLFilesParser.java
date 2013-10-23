package org.mule.tools.apikit.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionPair;
import org.mule.tools.apikit.output.GenerationModelProvider;
import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;

public class RAMLFilesParser
{

    private final GenerationModelProvider generationModelProvider;
    private Set<ResourceActionPair> entries = new HashSet<ResourceActionPair>();
    private final APIFactory apiFactory;

    public RAMLFilesParser(Log log, GenerationModelProvider generationModelProvider, Map<File, InputStream> fileStreams, APIFactory apiFactory)
    {
        this.generationModelProvider = generationModelProvider;
        this.apiFactory = apiFactory;
        List<File> processedFiles = new ArrayList<File>();
        for (Map.Entry<File, InputStream> fileInputStreamEntry : fileStreams.entrySet())
        {
            String content;
            try
            {
                content = IOUtils.toString(fileInputStreamEntry.getValue());
            }
            catch (IOException ioe)
            {
                log.info("Error loading file " + fileInputStreamEntry.getKey().getName());
                break;

            }
            ResourceLoader resourceLoader = new CompositeResourceLoader(new DefaultResourceLoader(), new FileResourceLoader(fileInputStreamEntry.getKey().getParentFile()));

            if (isValidYaml(fileInputStreamEntry.getKey().getName(), content, resourceLoader, log))
            {
                RamlDocumentBuilder builderNodeHandler = new RamlDocumentBuilder(resourceLoader);
                try
                {
                    Raml raml = builderNodeHandler.build(content);
                    collectResources(fileInputStreamEntry.getKey(), entries, raml.getResources(), API.DEFAULT_BASE_URI);
                    processedFiles.add(fileInputStreamEntry.getKey());
                }
                catch (Exception e)
                {
                    log.info("Could not parse [" + fileInputStreamEntry.getKey() + "] as root yaml file. Reason: " + e.getMessage());
                    log.debug(e);
                }
            }

        }
        if (processedFiles.size() > 0)
        {
            log.info("The following YAML files were parsed correctly: " +
                     processedFiles);
        }
        else
        {
            log.error("YAML Root not found. None of the files were recognized as root yaml files.");
        }
    }

    private boolean isValidYaml(String fileName, String content, ResourceLoader resourceLoader, Log log)
    {
        List<ValidationResult> validationResults = RamlValidationService.createDefault(resourceLoader).validate(content);
        if (validationResults != null && !validationResults.isEmpty())
        {
            log.info("File '" + fileName + "' is not a valid root yaml file. See following error(s): ");
            int errorCount = 1;
            for (ValidationResult validationResult : validationResults)
            {
                log.info("Error " + errorCount + ": " + validationResult.toString());
                errorCount++;
            }
            return false;
        }
        return true;
    }

    void collectResources(File filename, Set<ResourceActionPair> resources, Map<String, Resource> resourceMap, String baseUri)
    {
        for (Resource resource : resourceMap.values())
        {
            for (Action action : resource.getActions().values())
            {
                API api = apiFactory.createAPIBinding(filename, null, baseUri, null);
                String path = APIKitTools.getPathFromUri(baseUri);
                ResourceActionPair resourceActionPair = new ResourceActionPair(api, path + resource.getUri(),
                                                                               action.getType().toString());
                generationModelProvider.add(api, resourceActionPair, resource, action);
                resources.add(resourceActionPair);
            }

            collectResources(filename, resources, resource.getResources(), baseUri);
        }
    }

    public Set<ResourceActionPair> getEntries()
    {
        return entries;
    }
}
