/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.FileResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

public class RAMLFilesParser
{
    private Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
    private final APIFactory apiFactory;
    private final Log log;
    private final String muleVersion;

    public RAMLFilesParser(Log log, Map<File, InputStream> fileStreams, APIFactory apiFactory, String muleVersion)
    {
        this.log = log;
        this.apiFactory = apiFactory;
        this.muleVersion = muleVersion;
        List<File> processedFiles = new ArrayList<>();
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
                try
                {
                    IRaml raml = ParserV2Utils.build(resourceLoader, ramlFile.getPath(), content);

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

    private boolean isValidRaml(String fileName, String content, ResourceLoader resourceLoader)
    {
        List<String> errors = ParserV2Utils.validate(resourceLoader, fileName, content);
        if (!errors.isEmpty())
        {
            if (errors.size() == 1 && errors.get(0).toLowerCase().contains("root"))
            {
                log.info("File '" + fileName + "' is not a root RAML file.");
            }
            else
            {
                log.info("File '" + fileName + "' is not a valid root RAML file. It contains some errors/warnings. See below: ");
                int problemCount = 0;
                for (String error : errors)
                {
                    log.info("ERROR " + (++problemCount) + ": " + error);
                }
            }
            return false;
        }
        log.info("File '" + fileName + "' is a VALID root RAML file.");
        return true;
    }

    void collectResources(File filename, Map<String, IResource> resourceMap, String baseUri)
    {
        for (IResource resource : resourceMap.values())
        {
            for (IAction action : resource.getActions().values())
            {

                API api = apiFactory.createAPIBinding(filename,null, baseUri, APIKitTools.getPathFromUri(baseUri,false), null, null, APIKitTools.defaultIsInboundEndpoint(muleVersion));

                Map<String, IMimeType> mimeTypes = action.getBody();
                boolean addGenericAction = false;
                if (mimeTypes != null && !mimeTypes.isEmpty())
                {
                    for (IMimeType mimeType : mimeTypes.values())
                    {
                        if (mimeType.getSchema() != null || (mimeType.getFormParameters() != null && !mimeType.getFormParameters().isEmpty()))
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

    void addResource(API api, IResource resource, IAction action, String mimeType) {
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
