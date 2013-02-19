/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.action;

import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.apikit.api.Representation;
import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.RestWebService;
import org.mule.module.apikit.rest.UnexceptedErrorException;
import org.mule.module.apikit.rest.resource.ResourceNotFoundException;
import org.mule.transformer.types.MimeTypes;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.mule.util.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BaseUriRetrieveAction implements RestAction
{

    public static final String RESOURCE_BASE_PATH = "/org/mule/module/apikit/rest/swagger/";

    protected RestWebService restWebService;

    public BaseUriRetrieveAction(RestWebService restWebService)
    {
        this.restWebService = restWebService;
    }

    @Override
    public MuleEvent handle(RestRequest restRequest) throws RestException
    {
        String acceptContentTypes = restRequest.getProtocolAdaptor().getAcceptedContentTypes();

        if (acceptContentTypes.toLowerCase().contains("application/swagger+json"))
        {
            return swaggerJsonAction.handle(restRequest);
        }
        else if (acceptContentTypes.contains("text/html"))
        {
            return swaggerHtmlAction.handle(restRequest);
        }
        else
        {
            throw new MediaTypeNotAcceptableException();
        }
    }

    @Override
    public ActionType getType()
    {
        return ActionType.RETRIEVE;
    }

    static final RestAction swaggerJsonAction = new RestAction()
    {
        @Override
        public MuleEvent handle(RestRequest restRequest) throws RestException
        {
            try
            {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(restRequest.getInterface());
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
                    .setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, "application/swagger+json");
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

        @Override
        public ActionType getType()
        {
            return ActionType.RETRIEVE;
        }

        @Override
        public MessageProcessor getHandler()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getAccessExpression()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getDescription()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Representation getRepresentation()
        {
            // TODO Auto-generated method stub
            return null;
        }
    };

    static final RestAction swaggerHtmlAction = new RestAction()
    {
        @Override
        public MuleEvent handle(RestRequest restRequest) throws RestException
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
                    .toString()
                                                         + "/_swagger/");

                buffer = buffer.replace("${baseUrl}", restRequest.getMuleEvent()
                    .getMessageSourceURI()
                    .toString());

                buffer = buffer.replace("${resourcesJson}", restRequest.getMuleEvent()
                    .getMessageSourceURI()
                    .toString());

                buffer = buffer.replace("${pageTitle}", restRequest.getInterface().getName() + " UI");

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

        @Override
        public ActionType getType()
        {
            return ActionType.RETRIEVE;
        }

        @Override
        public MessageProcessor getHandler()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getAccessExpression()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getDescription()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Representation getRepresentation()
        {
            // TODO Auto-generated method stub
            return null;
        }
    };

    @Override
    public MessageProcessor getHandler()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAccessExpression()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Representation getRepresentation()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
