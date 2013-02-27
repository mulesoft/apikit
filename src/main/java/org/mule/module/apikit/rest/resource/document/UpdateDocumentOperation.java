
package org.mule.module.apikit.rest.resource.document;

import static org.mule.module.apikit.rest.operation.RestOperationType.UPDATE;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.ALLOW_MULTIPLE_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.BODY_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.DATA_TYPE_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.NAME_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.PARAMETERS_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.PARAM_TYPE_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.REQUIRED_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.RESPONSE_CLASS_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.SUPPORTED_CONTENT_TYPES_FIELD_NAME;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;
import org.mule.module.apikit.rest.swagger.SwaggerConstants;
import org.mule.module.apikit.rest.util.NameUtils;
import org.mule.transport.NullPayload;
import org.mule.util.StringUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public class UpdateDocumentOperation extends AbstractRestOperation
{

    public UpdateDocumentOperation()
    {
        this.type = UPDATE;
    }

    @Override
    public void handle(RestRequest request) throws RestException
    {
        super.handle(request);
        if (request.getMuleEvent() != null && request.getMuleEvent().getMessage() != null)
        {
            request.getMuleEvent().getMessage().setPayload(NullPayload.getInstance());
        }
    }

    @Override
    public void appendSwaggerDescriptor(JsonGenerator jsonGenerator)
        throws JsonGenerationException, IOException
    {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName(SwaggerConstants.HTTP_METHOD_FIELD_NAME);
        jsonGenerator.writeString("PUT");
        jsonGenerator.writeFieldName(SwaggerConstants.NICKNAME_FIELD_NAME);
        jsonGenerator.writeString("update" + StringUtils.capitalize(resource.getName()));

        jsonGenerator.writeFieldName(PARAMETERS_FIELD_NAME);
        jsonGenerator.writeStartArray();

        if (getAllRepresentations() != null)
        {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName(PARAM_TYPE_FIELD_NAME);
            jsonGenerator.writeString(BODY_FIELD_NAME);
            jsonGenerator.writeFieldName(NAME_FIELD_NAME);
            jsonGenerator.writeString("body");
            jsonGenerator.writeFieldName(DATA_TYPE_FIELD_NAME);
            jsonGenerator.writeString(NameUtils.singularize(StringUtils.capitalize(NameUtils.camel(resource.getName())))
                                      + "ToBeUpdated");
            jsonGenerator.writeFieldName(REQUIRED_FIELD_NAME);
            jsonGenerator.writeBoolean(true);
            jsonGenerator.writeFieldName(ALLOW_MULTIPLE_FIELD_NAME);
            jsonGenerator.writeBoolean(false);

            jsonGenerator.writeFieldName(SUPPORTED_CONTENT_TYPES_FIELD_NAME);
            jsonGenerator.writeStartArray();

            for (RepresentationMetaData representation : getAllRepresentations())
            {
                jsonGenerator.writeString(representation.getMediaType().toString());
            }

            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }

        jsonGenerator.writeFieldName("summary");
        jsonGenerator.writeString(getDescription());

        jsonGenerator.writeFieldName(RESPONSE_CLASS_FIELD_NAME);
        jsonGenerator.writeString("void");

        jsonGenerator.writeEndObject();
    }
}
