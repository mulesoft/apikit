/**
 * Mule Rest Module
 *
 * Copyright 2011-2012 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package org.mule.module.wsapi.rest.swagger.json;

import org.mule.module.wsapi.rest.RestWebServiceInterface;
import org.mule.module.wsapi.rest.resource.RestResource;
import org.mule.module.wsapi.rest.swagger.RestSwaggerConstants;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

public class RestWebServiceInterfaceSerializer extends JsonSerializer<RestWebServiceInterface>
{

    @Override
    public void serialize(RestWebServiceInterface restWebServiceInterface,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException, JsonProcessingException
    {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName("apiVersion");
        jsonGenerator.writeString("1.0");
        jsonGenerator.writeFieldName("swaggerVersion");
        jsonGenerator.writeString(RestSwaggerConstants.SWAGGER_VERSION);
        jsonGenerator.writeFieldName("basePath");
        jsonGenerator.writeString("{baseSwaggerUri}");
        jsonGenerator.writeFieldName("apis");
        jsonGenerator.writeStartArray();
        writeApiDeclarations((List<RestResource>) restWebServiceInterface.getRoutes(), jsonGenerator);
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }

    private void writeApiDeclarations(List<RestResource> resources, JsonGenerator jsonGenerator)
        throws IOException
    {
        for (RestResource resource : resources)
        {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("path");
            jsonGenerator.writeString("/" + resource.getName());
            jsonGenerator.writeFieldName("description");
            // jsonGenerator.writeString(resource.getDescription().trim());
            jsonGenerator.writeEndObject();
        }
    }
}
