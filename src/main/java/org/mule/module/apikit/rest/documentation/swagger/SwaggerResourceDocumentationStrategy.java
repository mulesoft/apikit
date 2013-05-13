/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.documentation.swagger;

import static org.mule.module.apikit.rest.swagger.SwaggerConstants.DESCRIPTION_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.MODELS_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.OPERATIONS_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.PATH_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.PROPERTIES_FIELD_NAME;

import org.mule.RequestContext;
import org.mule.api.MuleContext;
import org.mule.api.MuleRuntimeException;
import org.mule.api.registry.RegistrationException;
import org.mule.module.apikit.rest.RestDocumentationStrategy;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;
import org.mule.module.apikit.rest.resource.HierarchicalRestResource;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.module.apikit.rest.resource.base.BaseResource;
import org.mule.module.apikit.rest.resource.collection.CollectionMemberResource;
import org.mule.module.apikit.rest.resource.collection.CollectionResource;
import org.mule.module.apikit.rest.swagger.SwaggerConsoleResource;
import org.mule.module.apikit.rest.swagger.SwaggerConstants;
import org.mule.module.apikit.rest.util.NameUtils;
import org.mule.module.apikit.rest.validation.cache.JsonSchemaAndNode;
import org.mule.module.apikit.rest.validation.cache.JsonSchemaCache;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.net.MediaType;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import org.springframework.util.StringUtils;

public class SwaggerResourceDocumentationStrategy implements RestDocumentationStrategy
{

    protected boolean enableConsole = Boolean.TRUE;
    protected String consolePath = "console";
    
    public SwaggerResourceDocumentationStrategy()
    {
        super();
    }

    public SwaggerResourceDocumentationStrategy(boolean enableConsole, String conolePath)
    {
        super();
        this.enableConsole = enableConsole;
        this.consolePath = conolePath;
    }
    
    public MediaType getDocumentationMediaType()
    {
        return MediaType.JSON_UTF_8;
    }

    @Override
    public Object documentResource(RestResource resource, RestRequest request)
    {
        if (resource instanceof BaseResource)
        {
            return createSwaggerResourceListing((BaseResource) resource, request);
        }
        else
        {
            return createSwaggerAPIDeclaration(resource, request.getProtocolAdaptor().getBaseURI(), request);
        }
    }

    protected String createSwaggerAPIDeclaration(RestResource resource, URI baseURI, RestRequest request)
    {
        try
        {
            StringWriter writer = new StringWriter();
            JsonFactory jsonFactory = new JsonFactory();
            JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(writer);

            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("apiVersion");
            jsonGenerator.writeString(request.getService().getInterface().getVersion());
            jsonGenerator.writeFieldName("swaggerVersion");
            jsonGenerator.writeString(SwaggerConstants.SWAGGER_VERSION);
            jsonGenerator.writeFieldName("basePath");
            jsonGenerator.writeString("{baseSwaggerUri}");
            jsonGenerator.writeFieldName(SwaggerConstants.RESOURCE_PATH_FIELD_NAME);
            jsonGenerator.writeString(resource.getPath());
            jsonGenerator.writeFieldName(SwaggerConstants.APIS_FIELD_NAME);
            jsonGenerator.writeStartArray();

            List<SwaggerModel> swaggerModels = new ArrayList<SwaggerModel>();
            documentResource(jsonGenerator, resource, swaggerModels);
            jsonGenerator.writeEndArray();

            writeModelsFromSchema(swaggerModels,jsonGenerator);

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

    protected String createSwaggerResourceListing(BaseResource resource, RestRequest request)
    {
        try
        {
            StringWriter writer = new StringWriter();
            JsonFactory jsonFactory = new JsonFactory();
            JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(writer);

            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("apiVersion");
            jsonGenerator.writeString(request.getService().getInterface().getVersion());
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

    protected void documentResource(JsonGenerator jsonGenerator, RestResource resource, List<SwaggerModel> models)
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
            //TODO PLG fix the reason for this condition. Members are created through reference to the collection but the operation belongs to the collection member
            if (resource instanceof CollectionMemberResource && operation.getType().equals(RestOperationType.CREATE))
            {
                continue;
            }
            operation.appendSwaggerDescriptor(jsonGenerator);
        }
        //TODO PLG fix the reason for this condition. Same reason as
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
        for (RepresentationMetaData representationMetaData : resource.getRepresentations()) {
            if (representationMetaData.getMediaType().withoutParameters().equals(MediaType.JSON_UTF_8.withoutParameters()) && representationMetaData.getSchemaLocation() != null)
            {
                models.add(new SwaggerModel(representationMetaData));
            }
        }
        if (resource instanceof HierarchicalRestResource)
        {
            HierarchicalRestResource hierarchicalRestResource = (HierarchicalRestResource) resource;
            for (RestResource child : hierarchicalRestResource.getResources())
            {
                documentResource(jsonGenerator, child, models);
            }
        }
        if (resource instanceof CollectionResource)
        {
            documentResource(jsonGenerator, ((CollectionResource) resource).getMemberResource(), models);
        }
    }

    private void writeModelsFromSchema(List<SwaggerModel> models, JsonGenerator jsonGenerator) throws IOException {
        if (models.isEmpty())
        {
            return;
        }
        jsonGenerator.writeFieldName(MODELS_FIELD_NAME);
        jsonGenerator.writeStartObject();
        {
            for (SwaggerModel model : models) {
                Queue<Map.Entry<String, JsonNode>> innerObjects = new LinkedList<Map.Entry<String, JsonNode>>();

                try {
                    MuleContext muleContext = RequestContext.getEventContext().getMuleContext();
                    JsonSchemaAndNode jsonSchemaAndNode = JsonSchemaCache.getJsonSchemaCache(muleContext).get(model.getSchemaLocation());
                    JsonNode properties = jsonSchemaAndNode.getJsonNode();
                    innerObjects.add(new HashMap.SimpleEntry<String, JsonNode>(model.getName(), properties));
                } catch (ExecutionException e) {
                    throw new MuleRuntimeException(e);
                } catch (RegistrationException e) {
                    throw new MuleRuntimeException(e);
                }

                while (!innerObjects.isEmpty()) {
                    Map.Entry<String, JsonNode> currentObject = innerObjects.poll();

                    if( currentObject.getValue() == null || currentObject.getValue().get("properties") == null) {
                        continue;
                    }

                    jsonGenerator.writeFieldName(model.getName());
                    jsonGenerator.writeStartObject();
                    {

                        jsonGenerator.writeFieldName("id");
                        jsonGenerator.writeString(model.getName());

                        jsonGenerator.writeFieldName(PROPERTIES_FIELD_NAME);
                        jsonGenerator.writeStartObject();
                        {

                            JsonNode fields = currentObject.getValue().get("properties");
                            if (fields != null)
                            {
                                Iterator<String> fieldNames = fields.fieldNames();
                                while(fieldNames.hasNext())
                                {
                                    String fieldName = fieldNames.next();
                                    JsonNode field = fields.get(fieldName);
                                    jsonGenerator.writeFieldName(fieldName);

                                    String propertyType = field.get("type").asText();
                                    if ("array".equals(propertyType)) {
                                        String itemType = field.get("items").get("type").asText();
                                        if ("object".equals(itemType)) {
                                            jsonGenerator.writeStartObject();
                                            jsonGenerator.writeFieldName("type");
                                            jsonGenerator.writeString("array");
                                            jsonGenerator.writeFieldName("items");
                                            jsonGenerator.writeStartObject();
                                            jsonGenerator.writeFieldName("$ref");
                                            String innerObjectName = currentObject.getKey() + StringUtils.capitalize(NameUtils.singularize(fieldName));
                                            jsonGenerator.writeString(innerObjectName);
                                            innerObjects.add(new HashMap.SimpleEntry<String, JsonNode>(innerObjectName, field.get("items").get("properties")));
                                            jsonGenerator.writeEndObject();
                                            jsonGenerator.writeEndObject();
                                        } else {
                                            jsonGenerator.writeTree(field);
                                        }
                                    } else if ("object".equals(propertyType)) {
                                        jsonGenerator.writeStartObject();
                                        jsonGenerator.writeFieldName("type");
                                        String innerObjectName = currentObject.getKey() + StringUtils.capitalize(NameUtils.singularize(fieldName));
                                        jsonGenerator.writeString(model.getName());
                                        innerObjects.add(new HashMap.SimpleEntry<String, JsonNode>(innerObjectName, field.get("properties")));
                                        jsonGenerator.writeEndObject();
                                    } else {
                                        jsonGenerator.writeStartObject();
                                        jsonGenerator.writeFieldName("type");
                                        jsonGenerator.writeString(field.get("type").textValue());
                                        jsonGenerator.writeEndObject();
                                    }
                                }
                            }
                        }
                        jsonGenerator.writeEndObject();
                    }
                    jsonGenerator.writeEndObject();
                }
            }
        }
        jsonGenerator.writeEndObject();
    }

    public boolean isEnableConsole()
    {
        return enableConsole;
    }

    public String getConsolePath()
    {
        return consolePath;
    }
}
