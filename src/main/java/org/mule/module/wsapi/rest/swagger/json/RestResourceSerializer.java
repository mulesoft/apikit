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

import org.mule.module.wsapi.rest.resource.RestResource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class RestResourceSerializer extends JsonSerializer<RestResource>
{

    private static final String APIS_FIELD_NAME = "apis";
    private static final String MODELS_FIELD_NAME = "models";
    private static final String RESOURCE_PATH_FIELD_NAME = "resourcePath";
    private static final String BASE_PATH_FIELD_NAME = "basePath";
    private static final String SWAGGER_VERSION_FIELD_NAME = "swaggerVersion";
    private static final String API_VERSION_FIELD_NAME = "apiVersion";
    private static final String PATH_FIELD_NAME = "path";
    private static final String DESCRIPTION_FIELD_NAME = "description";
    private static final String OPERATIONS_FIELD_NAME = "operations";
    private static final String HTTP_METHOD_FIELD_NAME = "httpMethod";
    private static final String PARAM_TYPE_FIELD_NAME = "paramType";
    private static final String SUMMARY_FIELD_NAME = "summary";
    private static final String NOTES_FIELD_NAME = "notes";
    private static final String RESPONSE_CLASS_FIELD_NAME = "responseClass";
    private static final String ERROR_RESPONSES_FIELD_NAME = "errorResponses";
    private static final String CODE_FIELD_NAME = "code";
    private static final String REASON_FIELD_NAME = "reason";
    private static final String REQUIRED_FIELD_NAME = "required";
    private static final String ALLOW_MULTIPLE_FIELD_NAME = "allowMultiple";
    private static final String DATA_TYPE_FIELD_NAME = "dataType";
    private static final String NAME_FIELD_NAME = "name";
    private static final String DEFAULT_DATA_TYPE = "string";
    private static final String PROPERTIES_FIELD_NAME = "properties";
    private static final String SUPPORTED_CONTENT_TYPES_FIELD_NAME = "supportedContentTypes";
    private static final String BODY_FIELD_NAME = "body";
    private static final String PARAMETERS_FIELD_NAME = "parameters";
    private static final String NICKNAME_FIELD_NAME = "nickname";

    @Override
    public void serialize(RestResource resource, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }



}
