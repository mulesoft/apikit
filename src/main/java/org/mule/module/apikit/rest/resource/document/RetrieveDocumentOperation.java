
package org.mule.module.apikit.rest.resource.document;

import org.mule.module.apikit.rest.RestParameter;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;
import org.mule.module.apikit.rest.swagger.SwaggerConstants;
import org.mule.module.apikit.rest.util.NameUtils;
import org.mule.util.StringUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public class RetrieveDocumentOperation extends AbstractRestOperation
{

    public RetrieveDocumentOperation()
    {
        this.type = RestOperationType.RETRIEVE;
    }

    @Override
    public void appendSwaggerDescriptor(JsonGenerator jsonGenerator)
        throws JsonGenerationException, IOException
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
        jsonGenerator.writeEndArray();

        jsonGenerator.writeFieldName("summary");
        jsonGenerator.writeString(getDescription());

        jsonGenerator.writeFieldName(SwaggerConstants.RESPONSE_CLASS_FIELD_NAME);
        jsonGenerator.writeString(StringUtils.capitalize(NameUtils.camel(resource.getName())));

        jsonGenerator.writeEndObject();
    }

}
