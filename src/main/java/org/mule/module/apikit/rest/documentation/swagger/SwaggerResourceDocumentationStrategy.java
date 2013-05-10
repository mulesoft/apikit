/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.documentation.swagger;

import static org.mule.module.apikit.rest.swagger.SwaggerConstants.DESCRIPTION_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.OPERATIONS_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.PATH_FIELD_NAME;

import org.mule.api.MuleRuntimeException;
import org.mule.module.apikit.rest.RestDocumentationStrategy;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.HierarchicalRestResource;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.module.apikit.rest.resource.base.BaseResource;
import org.mule.module.apikit.rest.resource.collection.CollectionResource;
import org.mule.module.apikit.rest.swagger.SwaggerConsoleResource;
import org.mule.module.apikit.rest.swagger.SwaggerConstants;

import com.google.common.net.MediaType;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

public class SwaggerResourceDocumentationStrategy implements RestDocumentationStrategy
{

    public MediaType getDocumentationMediaType()
    {
        return MediaType.JSON_UTF_8;
    }

    @Override
    public Object documentResource(RestResource resource, RestRequest request)
    {
        if (resource instanceof BaseResource)
        {
            return createSwaggerResourceListing((BaseResource) resource);
        }
        else
        {
            return createSwaggerAPIDeclartion(resource, request.getProtocolAdaptor().getBaseURI());
        }
    }

    protected String createSwaggerAPIDeclartion(RestResource resource, URI baseURI)
    {
        try
        {
            StringWriter writer = new StringWriter();
            JsonFactory jsonFactory = new JsonFactory();
            JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(writer);

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

            documentResource(jsonGenerator, resource);

            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
            jsonGenerator.flush();

            String json = writer.toString();
            json = json.replace("{baseSwaggerUri}", baseURI.toString());

            return json;
        }
        catch (Exception e)
        {
            throw new MuleRuntimeException(e);
        }
    }

    protected String createSwaggerResourceListing(BaseResource resource)
    {
        try
        {
            StringWriter writer = new StringWriter();
            JsonFactory jsonFactory = new JsonFactory();
            JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(writer);

            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("apiVersion");
            jsonGenerator.writeString("1.0");
            jsonGenerator.writeFieldName("swaggerVersion");
            jsonGenerator.writeString(SwaggerConstants.SWAGGER_VERSION);
            jsonGenerator.writeFieldName(SwaggerConstants.APIS_FIELD_NAME);
            jsonGenerator.writeStartArray();

            for (RestResource child : resource.getResources())
            {
                if (!(child instanceof SwaggerConsoleResource))
                {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeFieldName(PATH_FIELD_NAME);
                    jsonGenerator.writeString(child.getPath());
                    jsonGenerator.writeFieldName(DESCRIPTION_FIELD_NAME);
                    jsonGenerator.writeString(child.getDescription().trim());
                    jsonGenerator.writeEndObject();
                }
            }

            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
            jsonGenerator.flush();

            return writer.toString();
        }
        catch (Exception e)
        {
            throw new MuleRuntimeException(e);
        }
    }

    protected void documentResource(JsonGenerator jsonGenerator, RestResource resource)
        throws JsonGenerationException, IOException
    {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName(PATH_FIELD_NAME);
        jsonGenerator.writeString(resource.getPath());
        jsonGenerator.writeFieldName(DESCRIPTION_FIELD_NAME);
        jsonGenerator.writeString(resource.getDescription().trim());
        jsonGenerator.writeFieldName(OPERATIONS_FIELD_NAME);
        jsonGenerator.writeStartArray();
        for (RestOperation operation : resource.getOperations())
        {
            operation.appendSwaggerDescriptor(jsonGenerator);
        }
        if (resource instanceof CollectionResource)
        {
            CollectionResource collectionResource = (CollectionResource) resource;
            if (collectionResource.getMemberResource().getOperation(RestOperationType.CREATE) != null)
            {
                collectionResource.getMemberResource()
                    .getOperation(RestOperationType.CREATE)
                    .appendSwaggerDescriptor(jsonGenerator);
            }
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
        if (resource instanceof HierarchicalRestResource)
        {
            HierarchicalRestResource hierarchicalRestResource = (HierarchicalRestResource) resource;
            for (RestResource child : hierarchicalRestResource.getResources())
            {
                documentResource(jsonGenerator, child);
            }
        }
        if (resource instanceof CollectionResource)
        {
            documentResource(jsonGenerator, ((CollectionResource) resource).getMemberResource());
        }
    }
}
