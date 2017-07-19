/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.mule.raml.implv1.ParserV1Utils;
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
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.loader.RootRamlResourceLoader;

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
            String rootRamlName = ramlFile.getName();
            String ramlFolderPath = null;
            if (ramlFile.getParentFile() != null)
            {
                ramlFolderPath = ramlFile.getParentFile().getPath();
            }
            if (isValidRaml(rootRamlName, content, ramlFolderPath))
            {
                try
                {
                    IRaml raml;
                    if (ParserV2Utils.useParserV2(content))
                    {
                        ResourceLoader resourceLoader = getResourceLoader(ramlFolderPath);
                        raml = ParserV2Utils.build(resourceLoader, ramlFile.getPath(), content);
                    }
                    else
                    {
                        raml = ParserV1Utils.build(content, ramlFolderPath, rootRamlName);
                    }

                    collectResources(ramlFile, raml.getResources(), API.DEFAULT_BASE_URI, raml.getVersion());
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

    private boolean isValidRaml(String fileName, String content, String filePath)
    {
        List<String> errors;
        if (ParserV2Utils.useParserV2(content))
        {
            ResourceLoader resourceLoader = getResourceLoader(filePath);
            errors = ParserV2Utils.validate(resourceLoader, fileName, content);
        }
        else
        {
            errors = ParserV1Utils.validate(filePath, fileName, content);
        }
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

    private ResourceLoader getResourceLoader(String filePath)
    {
        return new CompositeResourceLoader(new RootRamlResourceLoader(new File(filePath)), new DefaultResourceLoader());
    }

    void collectResources(File filename, Map<String, IResource> resourceMap, String baseUri, String version)
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
                            addResource(api, resource, action, mimeType.getType(), version);
                        }
                        else { addGenericAction = true; }
                    }
                }
                else { addGenericAction = true; }

                if (addGenericAction) {
                    addResource(api, resource, action, null, version);
                }
            }

            collectResources(filename, resource.getResources(), baseUri, version);
        }
    }

    void addResource(API api, IResource resource, IAction action, String mimeType, String version) {
        String completePath;
        if (!api.useInboundEndpoint() && api.getHttpListenerConfig() != null)
        {
            completePath = APIKitTools.getCompletePathFromBasePathAndPath(api.getHttpListenerConfig().getBasePath(), api.getPath());
        }
        else
        {
            completePath = api.getPath();
        }
        ResourceActionMimeTypeTriplet resourceActionTriplet = new ResourceActionMimeTypeTriplet(api, completePath + resource.getResolvedUri(version),
                    action.getType().toString(), mimeType);
        entries.put(resourceActionTriplet, new GenerationModel(api, version, resource, action, mimeType));
    }

    public Map<ResourceActionMimeTypeTriplet, GenerationModel> getEntries()
    {
        return entries;
    }

}
