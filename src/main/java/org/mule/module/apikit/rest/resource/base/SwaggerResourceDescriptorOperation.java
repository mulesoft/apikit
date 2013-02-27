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

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.swagger.SwaggerConstants;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.StringWriter;

public class SwaggerResourceDescriptorOperation extends AbstractRestOperation
{

    public SwaggerResourceDescriptorOperation()
    {
        this.type = RestOperationType.RETRIEVE;
    }

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
            jsonGenerator.writeFieldName(SwaggerConstants.RESOURCE_PATH_FIELD_NAME);
            jsonGenerator.writeString(resource.getPath());
            jsonGenerator.writeFieldName(SwaggerConstants.APIS_FIELD_NAME);
            jsonGenerator.writeStartArray();

            resource.appendSwaggerJson(jsonGenerator);

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
                .setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY, String.valueOf(HttpConstants.SC_OK));
            restRequest.getMuleEvent()
                .getMessage()
                .setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, "application/swagger+json");
            restRequest.getMuleEvent()
                .getMessage()
                .setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, json.length());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
