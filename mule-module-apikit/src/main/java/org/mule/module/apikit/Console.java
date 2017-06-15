/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.extension.http.api.HttpHeaders;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.helpers.EventHelper;
import org.mule.module.apikit.helpers.EventWrapper;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.processor.Processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Console implements Processor
{
    @Inject
    private ApikitRegistry registry;
    private Configuration config;

    private static final String RAML_LOCATION_PLACEHOLDER_KEY = "RAML_LOCATION_PLACEHOLDER";

    private static final String CONSOLE_RESOURCES_BASE = "/console-resources";
    private static final String ROOT_CONSOLE_PATH = "/";
    private static final String INDEX_RESOURCE_RELATIVE_PATH = "/index.html";

    private String configRef;
    private String name;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Event process(Event event) throws MuleException
    {
        config = registry.getConfiguration(getConfigRef());
        EventWrapper eventWrapper = new EventWrapper(event, config.getOutboundHeadersMapName(), config.getHttpStatusVarName());

        HttpRequestAttributes attributes = EventHelper.getHttpRequestAttributes(event);

        // Listener path MUST end with /*
        validateConsoleListenerPath(attributes.getListenerPath());

        String consoleBasePath = UrlUtils.getBasePath(attributes);
        String resourceRelativePath = UrlUtils.getRelativePath(attributes);

        // If the request was made to, for example, /console, we must redirect the client to /console/
        if (!consoleBasePath.endsWith("/"))
        {
            eventWrapper.doClientRedirect();
            return eventWrapper.build();
        }

        // For getting RAML resources
        String raml = getRamlResourceIfRequested(EventHelper.getHttpRequestAttributes(event), resourceRelativePath);
        if (raml != null)
        {
            return eventWrapper.setPayload(raml, RamlHandler.APPLICATION_RAML).build();
        }

        return getConsoleResource(eventWrapper, resourceRelativePath);
    }

    private Event getConsoleResource(EventWrapper eventWrapper, String resourceRelativePath)
    {
        String consoleResourcePath;
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] buffer = null;
        boolean updateConsoleIndex = false;

        try
        {
            if (resourceRelativePath.equals(ROOT_CONSOLE_PATH))
            {
                consoleResourcePath = CONSOLE_RESOURCES_BASE + INDEX_RESOURCE_RELATIVE_PATH;
                updateConsoleIndex = true;
            }
            else
            {
                consoleResourcePath = CONSOLE_RESOURCES_BASE + resourceRelativePath;
            }

            inputStream = getClass().getResourceAsStream(consoleResourcePath);

            if (inputStream == null)
            {
                // If the file is not under the console-resources folder, then the request might be a RAML resource
                String raml = config.getRamlHandler().getRamlV2(resourceRelativePath);
                if (raml == null)
                {
                    throw ApikitErrorTypes.throwErrorType(new NotFoundException(resourceRelativePath));
                }
                return eventWrapper.setPayload(raml, RamlHandler.APPLICATION_RAML).build();
            }

            if (updateConsoleIndex) {
                inputStream = updateIndexWithRamlLocation(inputStream);
            }

            byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copyLarge(inputStream, byteArrayOutputStream);
            buffer = byteArrayOutputStream.toByteArray();

        }
        catch (IOException e)
        {
            throw ApikitErrorTypes.throwErrorType(new NotFoundException(resourceRelativePath));
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(byteArrayOutputStream);
        }
        MediaType mediaType = getRequestMediaType(consoleResourcePath);
        eventWrapper.setPayload(buffer, mediaType);
        // Adds necessary headers for the output event
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        if(mediaType.equals(MediaType.HTML))
        {
            headers.put(HttpHeaders.Names.EXPIRES, "-1");
        }
        eventWrapper.addOutboundProperties(headers);
        return eventWrapper.build();
    }

    private String getRamlResourceIfRequested(HttpRequestAttributes attributes, String resourceRelativePath)
    {
        if (config.getRamlHandler().isRequestingRamlV1ForConsole(attributes))
        {
            return config.getRamlHandler().getRamlV1();
        }

        if (config.getRamlHandler().isRequestingRamlV2(attributes))
        {
            return config.getRamlHandler().getRamlV2(resourceRelativePath);
        }

        return null;
    }



    /**
     * Updates index file with the location of the root Raml, so it can load it later.
     * @param inputStream
     * @return The inputStream of the modified file
     * @throws IOException
     */
    private InputStream updateIndexWithRamlLocation(InputStream inputStream) throws IOException
    {
        String ramlLocation;
        if (config.getRamlHandler().isParserV2())
        {
            ramlLocation = config.getRamlHandler().getRootRamlLocationForV2();
        }
        else
        {
            ramlLocation = config.getRamlHandler().getRootRamlLocationForV1();
        }


        String indexHtml = IOUtils.toString(inputStream);
        IOUtils.closeQuietly(inputStream);
        indexHtml = indexHtml.replaceFirst(RAML_LOCATION_PLACEHOLDER_KEY, ramlLocation);
        inputStream = new ByteArrayInputStream(indexHtml.getBytes());

        return inputStream;
    }


    /**
     * Validates if the path specified in the listener is a valid one. In order to this to be valid,
     * path MUST end with "/*". Example: path="/whatever/your/path/is/*"
     *
     * @param listenerPath Path specified in the listener element of the console
     */
    private void validateConsoleListenerPath(String listenerPath)
    {
        if (listenerPath != null && !listenerPath.endsWith("/*"))
        {
            throw new IllegalStateException("Console path in listener must end with /*");
        }
    }

    /**
     * Gets Media-Type according to the type of the file we have to send back
     * @param path
     * @return The MediaType corresponding to the path
     */
    private MediaType getRequestMediaType(String path)
    {
        String extension = FilenameUtils.getExtension(path);

        if (extension.endsWith("html")) return MediaType.HTML;
        if (extension.endsWith("js")) return MediaType.create("application","x-javascript");

        // Default MediaType
        return MediaType.BINARY;
    }




    public String getConfigRef()
    {
        return configRef;
    }

    public void setConfigRef(String config)
    {
        this.configRef = config;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
