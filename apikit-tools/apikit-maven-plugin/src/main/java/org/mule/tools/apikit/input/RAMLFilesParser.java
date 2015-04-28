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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationModel;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.rule.ValidationResult.Level;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;

public class RAMLFilesParser
{
    private Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
    private final APIFactory apiFactory;
    private final Log log;

    public RAMLFilesParser(Log log, Map<File, InputStream> fileStreams, APIFactory apiFactory)
    {
        this.log = log;
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
                this.log.info("Error loading file " + ramlFile.getName());
                break;

            }
            ResourceLoader resourceLoader = new CompositeResourceLoader(new DefaultResourceLoader(), new FileResourceLoader(ramlFile.getParentFile()));

            if (isValidRaml(ramlFile.getName(), content, resourceLoader))
            {
                RamlDocumentBuilder builderNodeHandler = new RamlDocumentBuilder(resourceLoader);
                try
                {
                    Raml raml = builderNodeHandler.build(content, ramlFile.getName());

                    collectResources(ramlFile, raml.getResources(), API.DEFAULT_BASE_URI);
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
            this.log.info("The following RAML files were parsed correctly: " +
                     processedFiles);
        }
        else
        {
            this.log.error("RAML Root not found. None of the files were recognized as valid root RAML files.");
        }
    }

    public boolean isValidRaml(String fileName, String content, ResourceLoader resourceLoader)
    {
        List<ValidationResult> validationResults = RamlValidationService.createDefault(resourceLoader).validate(content, fileName);
        if (validationResults != null && !validationResults.isEmpty())
        {
            log.info("File '" + fileName + "' is not a valid root RAML file. It contains some errors/warnings. See below: ");
            int errorsFound = findProblems(fileName, validationResults, Level.ERROR);
            //log warnings
            findProblems(fileName, validationResults, Level.WARN);
            if (errorsFound > 0) {
                return false;
            }
        }
        return true;
    }

    private int findProblems(String fileName, List<ValidationResult> validationResults, Level problemLevel)
    {
        int problemCount = 0;
        for (ValidationResult validationResult : validationResults)
        {
            if (validationResult.getLevel() == problemLevel)
            {
                log.info(problemLevel.name() + " " + (++problemCount) + ": " + validationResult.toString());
            } 
        }
        return problemCount;
    }

    void collectResources(File filename, Map<String, Resource> resourceMap, String baseUri)
    {
        for (Resource resource : resourceMap.values())
        {
            for (Action action : resource.getActions().values())
            {

                API api = apiFactory.createAPIBinding(filename,null, baseUri, APIKitTools.getPathFromUri(baseUri,false), null, null, false);

                Map<String, MimeType> mimeTypes = action.getBody();
                boolean addGenericAction = false;
                if (mimeTypes != null)
                {
                    for (MimeType mimeType : mimeTypes.values())
                    {
                        if (mimeType.getSchema() != null || mimeType.getFormParameters() != null)
                        {
                            addResource(api, resource, action, mimeType.getType());
                        }
                        else { addGenericAction = true; }
                    }
                }
                else { addGenericAction = true; }

                if (addGenericAction) {
                    addResource(api, resource, action, null);
                }
            }

            collectResources(filename, resource.getResources(), baseUri);
        }
    }

    void addResource(API api, Resource resource, Action action, String mimeType) {
        String completePath;
        if (!api.useInboundEndpoint() && api.getHttpListenerConfig() != null)
        {
            completePath = APIKitTools.getCompletePathFromBasePathAndPath(api.getHttpListenerConfig().getBasePath(), api.getPath());
        }
        else
        {
            completePath = api.getPath();
        }
        ResourceActionMimeTypeTriplet resourceActionTriplet = new ResourceActionMimeTypeTriplet(api, completePath + resource.getUri(),
            action.getType().toString(), mimeType);
        entries.put(resourceActionTriplet, new GenerationModel(api, resource, action, mimeType));
    }

    public Map<ResourceActionMimeTypeTriplet, GenerationModel> getEntries()
    {
        return entries;
    }
}
