/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
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
            File ramlFile = fileInputStreamEntry.getKey();
            try
            {
                content = IOUtils.toString(fileInputStreamEntry.getValue());
            }
            catch (IOException ioe)
            {
                log.info("Error loading file " + ramlFile.getName());
                break;

            }
            ResourceLoader resourceLoader = new CompositeResourceLoader(new DefaultResourceLoader(), new FileResourceLoader(ramlFile.getParentFile()));

            if (isValidYaml(ramlFile.getName(), content, resourceLoader, log))
            {
                RamlDocumentBuilder builderNodeHandler = new RamlDocumentBuilder(resourceLoader);
                try
                {
                    Raml raml = builderNodeHandler.build(content, ramlFile.getName());
                    collectResources(ramlFile, entries, raml.getResources(), API.DEFAULT_BASE_URI);
                    processedFiles.add(ramlFile);
                }
                catch (Exception e)
                {
                    log.info("Could not parse [" + ramlFile + "] as root RAML file. Reason: " + e.getMessage());
                    log.debug(e);
                }
            }

        }
        if (processedFiles.size() > 0)
        {
            log.info("The following RAML files were parsed correctly: " +
                     processedFiles);
        }
        else
        {
            log.error("RAML Root not found. None of the files were recognized as valid root RAML files.");
        }
    }

    private boolean isValidYaml(String fileName, String content, ResourceLoader resourceLoader, Log log)
    {
        List<ValidationResult> validationResults = RamlValidationService.createDefault(resourceLoader).validate(content, fileName);
        if (validationResults != null && !validationResults.isEmpty())
        {
            log.info("File '" + fileName + "' is not a valid root RAML file. See following error(s): ");
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
