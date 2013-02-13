/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.rest.action;

import org.mule.api.MuleEvent;
import org.mule.module.wsapi.rest.RestException;
import org.mule.module.wsapi.rest.RestRequest;
import org.mule.module.wsapi.rest.RestWebService;
import org.mule.module.wsapi.rest.UnexceptedErrorException;
import org.mule.module.wsapi.rest.protocol.MediaTypeNotAcceptable;
import org.mule.module.wsapi.rest.resource.ResourceNotFoundException;
import org.mule.transformer.types.MimeTypes;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.mule.util.FilenameUtils;
import org.mule.util.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BaseUriRetrieveAction implements RestAction
{

    public static final String RESOURCE_BASE_PATH = "/org/mule/modules/rest/swagger/";
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String MIME_TYPE_JAVASCRIPT = "application/x-javascript";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String MIME_TYPE_GIF = "image/gif";
    public static final String MIME_TYPE_CSS = "text/css";

    protected RestWebService restWebService;

    public BaseUriRetrieveAction(RestWebService restWebService)
    {
        this.restWebService = restWebService;
    }

    @Override
    public MuleEvent handle(RestRequest restRequest) throws RestException
    {
        String path = restRequest.getMuleEvent()
            .getMessage()
            .getInboundProperty(HttpConnector.HTTP_REQUEST_PATH_PROPERTY);
        if (restRequest.getProtocolAdaptor()
            .getAcceptedContentTypes()
            .equalsIgnoreCase("application/swagger+json")
            || restRequest.getProtocolAdaptor().getAcceptedContentTypes().equalsIgnoreCase("*/*"))
        {
            try
            {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(restWebService.getInterface());
                json = json.replace("{baseSwaggerUri}", restRequest.getMuleEvent()
                    .getMessageSourceURI()
                    .toString());

                restRequest.getMuleEvent().getMessage().setPayload(json);
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                        String.valueOf(HttpConstants.SC_OK));
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, MimeTypes.JSON);
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, json.length());
                return restRequest.getMuleEvent();

            }
            catch (JsonProcessingException e)
            {
                throw new UnexceptedErrorException(e);
            }
        }
        else if (restRequest.getProtocolAdaptor().getAcceptedContentTypes().contains("text/html"))
        {
            InputStream in = null;
            try
            {
                in = getClass().getResourceAsStream(RESOURCE_BASE_PATH + "index.html");
                if (in == null)
                {
                    throw new ResourceNotFoundException("index.html");
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copyLarge(in, baos);

                String buffer = new String(baos.toByteArray());

                buffer = buffer.replace("${swaggerUrl}", restRequest.getMuleEvent()
                    .getMessageSourceURI()
                    .toString());

                // urlBuilder.append("/resources.json");
                //
                // buffer = buffer.replace("${resourcesJson}", urlBuilder.toString());

                buffer = buffer.replace("${pageTitle}", restWebService.getInterface().getName() + " UI");

                restRequest.getMuleEvent().getMessage().setPayload(buffer);
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                        String.valueOf(HttpConstants.SC_OK));
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, MimeTypes.HTML);
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, buffer.length());
                return restRequest.getMuleEvent();
            }
            catch (JsonProcessingException e)
            {
                throw new UnexceptedErrorException(e);
            }
            catch (IOException e)
            {
                throw new UnexceptedErrorException(e);
            }
        }
        else if (path.endsWith(".png") || path.endsWith(".js") || path.endsWith(".css")
                 || path.endsWith(".html") || path.endsWith(".gif"))
        {
            InputStream in = null;
            try
            {
                in = getClass().getResourceAsStream(RESOURCE_BASE_PATH + path);
                if (in == null)
                {
                    restRequest.getMuleEvent().getMessage().setOutboundProperty("http.status", 404);
                    throw new ResourceNotFoundException(path);
                }

                String mimeType = DEFAULT_MIME_TYPE;
                if (FilenameUtils.getExtension(path).equals("html"))
                {
                    mimeType = MimeTypes.HTML;
                }
                else if (FilenameUtils.getExtension(path).equals("js"))
                {
                    mimeType = MIME_TYPE_JAVASCRIPT;
                }
                else if (FilenameUtils.getExtension(path).equals("png"))
                {
                    mimeType = MIME_TYPE_PNG;
                }
                else if (FilenameUtils.getExtension(path).equals("gif"))
                {
                    mimeType = MIME_TYPE_GIF;
                }
                else if (FilenameUtils.getExtension(path).equals("css"))
                {
                    mimeType = MIME_TYPE_CSS;
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copyLarge(in, baos);

                byte[] buffer = baos.toByteArray();

                restRequest.getMuleEvent().getMessage().setPayload(buffer);
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                        String.valueOf(HttpConstants.SC_OK));
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, mimeType);
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, buffer.length);
            }
            catch (IOException e)
            {
                throw new ResourceNotFoundException(path);
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        throw new UnexceptedErrorException(e);
                    }
                }
            }

            return restRequest.getMuleEvent();

        }
        else
        {
            throw new MediaTypeNotAcceptable();
        }
    }

    @Override
    public ActionType getType()
    {
        return ActionType.RETRIEVE;
    }
}
