/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.resource.base;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.apikit.api.WebServiceRoute;
import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.OperationHandlerException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.RestWebService;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.ResourceNotFoundException;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.module.apikit.rest.swagger.SwaggerConstants;
import org.mule.module.apikit.rest.util.RestContentTypeParser;
import org.mule.transformer.types.MimeTypes;
import org.mule.transport.NullPayload;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.mule.util.IOUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

public class BaseUriRetrieveOperation extends AbstractRestOperation
{

    public static final String RESOURCE_BASE_PATH = "/org/mule/module/apikit/rest/swagger/";

    protected RestWebService restWebService;

    public BaseUriRetrieveOperation(RestWebService restWebService)
    {
        this.restWebService = restWebService;
    }

    @Override
    public void handle(RestRequest restRequest) throws RestException
    {
        List<MediaType> acceptContentTypes = restRequest.getProtocolAdaptor()
            .getAcceptableResponseMediaTypes();

        if (RestContentTypeParser.isMediaTypeAcceptable(acceptContentTypes,
            MediaType.parse("application/swagger+json")))
        {
            swaggerJsonAction.handle(restRequest);
        }
        else if (RestContentTypeParser.isMediaTypeAcceptable(acceptContentTypes, MediaType.parse("text/html")))
        {
            swaggerHtmlAction.handle(restRequest);
        }
        else
        {
            throw new MediaTypeNotAcceptableException();
        }
    }

    @Override
    public MessageProcessor getHandler()
    {
        return new MessageProcessor()
        {
            @Override
            public MuleEvent process(MuleEvent event) throws MuleException
            {
                return new DefaultMuleEvent(new DefaultMuleMessage(NullPayload.getInstance(),
                    event.getMuleContext()), event);
            }
        };
    }

    @Override
    public RestOperationType getType()
    {
        return RestOperationType.RETRIEVE;
    }

    final RestOperation swaggerJsonAction = new AbstractRestOperation()
    {
        @Override
        public void handle(RestRequest restRequest) throws RestException
        {
            try
            {
                StringWriter writer = new StringWriter();
                JsonFactory jsonFactory = new JsonFactory();
                JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer);

                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("apiVersion");
                jsonGenerator.writeString("1.0");
                jsonGenerator.writeFieldName("swaggerVersion");
                jsonGenerator.writeString(SwaggerConstants.SWAGGER_VERSION);
                jsonGenerator.writeFieldName("basePath");
                jsonGenerator.writeString("{baseSwaggerUri}");
                jsonGenerator.writeFieldName("apis");
                jsonGenerator.writeStartArray();
                for (WebServiceRoute resource : restWebService.getInterface().getRoutes())
                {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeFieldName("path");
                    jsonGenerator.writeString("/" + ((RestResource) resource).getName());
                    jsonGenerator.writeFieldName("description");
                    jsonGenerator.writeString(resource.getDescription().trim());
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
                jsonGenerator.flush();

                String json = writer.toString();
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

            }
            catch (Exception e)
            {
                throw new OperationHandlerException(e);
            }
        }

        @Override
        public RestOperationType getType()
        {
            return RestOperationType.RETRIEVE;
        }
    };

    static final RestOperation swaggerHtmlAction = new AbstractRestOperation()
    {
        @Override
        public void handle(RestRequest restRequest) throws RestException
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
                    .getMessage()
                    .getInboundProperty(HttpConnector.HTTP_REQUEST_PATH_PROPERTY)
                                                         + "/_swagger/");

                buffer = buffer.replace("${baseUrl}", restRequest.getMuleEvent()
                    .getMessageSourceURI()
                    .toString());

                buffer = buffer.replace("${resourcesJson}", restRequest.getMuleEvent()
                    .getMessageSourceURI()
                    .toString());

                buffer = buffer.replace("${pageTitle}", restRequest.getService().getInterface().getName()
                                                        + " UI");

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

            }
            catch (JsonProcessingException e)
            {
                throw new OperationHandlerException(e);
            }
            catch (IOException e)
            {
                throw new OperationHandlerException(e);
            }

        }

        @Override
        public RestOperationType getType()
        {
            return RestOperationType.RETRIEVE;
        }

    };

}
