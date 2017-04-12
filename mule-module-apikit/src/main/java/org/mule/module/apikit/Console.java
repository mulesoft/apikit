/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.processor.Processor;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Console implements Processor
{
    @Inject
    private ApikitRegistry registry;

    private static final String BASE_RESOURCE_FOLDER = "/console";
    private static final String ROOT_CONSOLE_PATH = "/";
    private static final String INDEX_RESOURCE_RELATIVE_PATH = "/index.html";

    private static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN_KEY = "Access-Control-Allow-Origin";
    private static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN_VALUE = "*";
    private static final String HEADER_EXPIRES_KEY = "Expires";
    private static final String HEADER_EXPIRES_VALUE = "-1";

    private String configRef;
    private String name;

    @Override
    public Event process(Event event) throws MuleException
    {
        Configuration config = registry.getConfiguration(getConfigRef());
        event = addEventVariables(event, config);

        HttpRequestAttributes attributes = ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());

        // Listener path MUST end with /*
        validateConsoleListenerPath(attributes.getListenerPath());
        String resourcePath = UrlUtils.getRelativePath(attributes);

        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] buffer;

        try
        {
            if (resourcePath.equals(ROOT_CONSOLE_PATH))
            {
                resourcePath = BASE_RESOURCE_FOLDER + INDEX_RESOURCE_RELATIVE_PATH;
            }
            else
            {
                resourcePath = BASE_RESOURCE_FOLDER + resourcePath;
            }

            inputStream = getClass().getResourceAsStream(resourcePath);

            if (inputStream == null)
            {
                throw new NotFoundException(resourcePath);
            }

            byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copyLarge(inputStream, byteArrayOutputStream);
            buffer = byteArrayOutputStream.toByteArray();

        }
        catch (IOException e)
        {
            throw new NotFoundException(resourcePath);
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(byteArrayOutputStream);
        }

        return getOutputEvent(event, config.getOutboundHeadersMapName(), buffer, getRequestMediaType(resourcePath));
    }

    private Event addEventVariables(Event event, Configuration config)
    {
        event = EventHelper.addVariable(event, config.getOutboundHeadersMapName(), new HashMap<>());
        event = EventHelper.addVariable(event, config.getHttpStatusVarName(), "200");
        return event;
    }

    /**
     * Creates the output event containing the data that must be sent in the response
     *
     * @param inputEvent
     * @param outboundHeadersMapName
     * @param payload Payload to be sent
     * @param mediaType Payload's Media Type
     * @return
     */
    private Event getOutputEvent(Event inputEvent, String outboundHeadersMapName, byte[] payload, MediaType mediaType)
    {
        Message.Builder messageBuilder = Message.builder(inputEvent.getMessage());
        messageBuilder.mediaType(mediaType);
        messageBuilder.payload(payload);

        Event.Builder eventBuilder = Event.builder(inputEvent);
        eventBuilder.message(messageBuilder.build());
        inputEvent = eventBuilder.build();

        // Adds necessary headers for the output event
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN_KEY, HEADER_ACCESS_CONTROL_ALLOW_ORIGIN_VALUE);
        if(mediaType.equals(MediaType.HTML))
        {
            headers.put(HEADER_EXPIRES_KEY, HEADER_EXPIRES_VALUE);
        }
        inputEvent = EventHelper.addOutboundProperties(inputEvent, outboundHeadersMapName, headers);

        return inputEvent;
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
     *
     * @param path
     * @return The MediaType corresponding to the path
     */
    private MediaType getRequestMediaType(String path)
    {
        String extension = FilenameUtils.getExtension(path);

        if (extension.endsWith("html")) return MediaType.HTML;
        if (extension.endsWith("js")) return MediaType.create("application","x-javascript");
        if (extension.endsWith("css")) return MediaType.create("text","css");
        if (extension.endsWith("png")) return MediaType.create("image", "png");
        if (extension.endsWith("gif")) return MediaType.create("image", "gif");
        if (extension.endsWith("svg")) return MediaType.create("image", "svg+xml");
        if (extension.endsWith("raml")) return MediaType.create("applicatio", "raml+yaml");

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
