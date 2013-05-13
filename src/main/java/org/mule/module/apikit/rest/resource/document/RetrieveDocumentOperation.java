/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.rest.resource.document;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.param.RestParameter;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;
import org.mule.module.apikit.rest.swagger.SwaggerConstants;
import org.mule.module.apikit.rest.util.NameUtils;
import org.mule.util.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public class RetrieveDocumentOperation extends AbstractRestOperation
{

    public RetrieveDocumentOperation()
    {
        this.type = RestOperationType.RETRIEVE;
    }

    @Override
    public void appendSwaggerDescriptor(JsonGenerator jsonGenerator) throws IOException
    {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName(SwaggerConstants.HTTP_METHOD_FIELD_NAME);
        jsonGenerator.writeString("GET");
        jsonGenerator.writeFieldName(SwaggerConstants.NICKNAME_FIELD_NAME);
        jsonGenerator.writeString("retrieve" + StringUtils.capitalize(resource.getName()));
        jsonGenerator.writeFieldName(SwaggerConstants.DESCRIPTION_FIELD_NAME);
        jsonGenerator.writeString(getDescription());

        if (getAllRepresentations() != null)
        {
            jsonGenerator.writeFieldName(SwaggerConstants.SUPPORTED_CONTENT_TYPES_FIELD_NAME);
            jsonGenerator.writeStartArray();
            for (RepresentationMetaData representation : getAllRepresentations())
            {
                jsonGenerator.writeString(representation.getMediaType().toString());
            }
            jsonGenerator.writeEndArray();
        }

        jsonGenerator.writeFieldName(SwaggerConstants.PARAMETERS_FIELD_NAME);
        jsonGenerator.writeStartArray();
        for (RestParameter param : resource.getParameters())
        {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName(SwaggerConstants.PARAM_TYPE_FIELD_NAME);
            jsonGenerator.writeString(SwaggerConstants.PATH_FIELD_NAME);
            jsonGenerator.writeFieldName(SwaggerConstants.NAME_FIELD_NAME);
            jsonGenerator.writeString(param.getName());
            jsonGenerator.writeFieldName(SwaggerConstants.DESCRIPTION_FIELD_NAME);
            jsonGenerator.writeString(param.getDescription());
            jsonGenerator.writeFieldName(SwaggerConstants.DATA_TYPE_FIELD_NAME);
            jsonGenerator.writeString(SwaggerConstants.DEFAULT_DATA_TYPE);
            jsonGenerator.writeFieldName(SwaggerConstants.REQUIRED_FIELD_NAME);
            jsonGenerator.writeBoolean(true);
            jsonGenerator.writeFieldName(SwaggerConstants.ALLOW_MULTIPLE_FIELD_NAME);
            jsonGenerator.writeBoolean(false);
            jsonGenerator.writeEndObject();
        }

        for (RestParameter param : getParameters())
        {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName(SwaggerConstants.PARAM_TYPE_FIELD_NAME);
            jsonGenerator.writeString(SwaggerConstants.QUERY_PARAMETER_TYPE);
            jsonGenerator.writeFieldName(SwaggerConstants.NAME_FIELD_NAME);
            jsonGenerator.writeString(param.getName());
            jsonGenerator.writeFieldName(SwaggerConstants.DESCRIPTION_FIELD_NAME);
            jsonGenerator.writeString(param.getDescription());
            jsonGenerator.writeFieldName(SwaggerConstants.DATA_TYPE_FIELD_NAME);
            jsonGenerator.writeString(SwaggerConstants.DEFAULT_DATA_TYPE);
            jsonGenerator.writeFieldName(SwaggerConstants.REQUIRED_FIELD_NAME);
            jsonGenerator.writeBoolean(false);
            jsonGenerator.writeFieldName(SwaggerConstants.ALLOW_MULTIPLE_FIELD_NAME);
            jsonGenerator.writeBoolean(true);
            jsonGenerator.writeEndObject();
        }

        jsonGenerator.writeEndArray();

        jsonGenerator.writeFieldName("summary");
        jsonGenerator.writeString(getDescription());

        jsonGenerator.writeFieldName(SwaggerConstants.RESPONSE_CLASS_FIELD_NAME);
        jsonGenerator.writeString(StringUtils.capitalize(NameUtils.camel(resource.getName())));

        jsonGenerator.writeEndObject();
    }

}
