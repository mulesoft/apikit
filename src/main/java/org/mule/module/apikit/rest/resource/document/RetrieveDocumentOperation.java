
package org.mule.module.apikit.rest.resource.document;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;
import org.mule.module.apikit.rest.resource.collection.CollectionMemberResource;
import org.mule.module.apikit.rest.swagger.SwaggerConstants;
import org.mule.module.apikit.rest.util.NameUtils;
import org.mule.module.apikit.rest.util.RestContentTypeParser;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.mule.util.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.net.MediaType;

import java.io.IOException;
import java.io.StringWriter;

public class RetrieveDocumentOperation extends AbstractRestOperation
{

    public RetrieveDocumentOperation()
    {
        this.type = RestOperationType.RETRIEVE;
    }

    @Override
    public void handle(RestRequest restRequest) throws RestException
    {
        if (RestContentTypeParser.isMediaTypeAcceptable(restRequest.getProtocolAdaptor()
            .getAcceptableResponseMediaTypes(), MediaType.create("application", "swagger+json")))
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
                e.printStackTrace();
            }
        }
        else
        {
            super.handle(restRequest);
        }
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

        if (resource instanceof CollectionMemberResource)
        {
            jsonGenerator.writeFieldName(SwaggerConstants.PARAMETERS_FIELD_NAME);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName(SwaggerConstants.PARAM_TYPE_FIELD_NAME);
            jsonGenerator.writeString(SwaggerConstants.PATH_FIELD_NAME);
            jsonGenerator.writeFieldName(SwaggerConstants.NAME_FIELD_NAME);
            jsonGenerator.writeString(resource.getName() + "Id");
            jsonGenerator.writeFieldName(SwaggerConstants.DESCRIPTION_FIELD_NAME);
            jsonGenerator.writeString("The id uses to identify " + resource.getName());
            jsonGenerator.writeFieldName(SwaggerConstants.DATA_TYPE_FIELD_NAME);
            jsonGenerator.writeString(SwaggerConstants.DEFAULT_DATA_TYPE);
            jsonGenerator.writeFieldName(SwaggerConstants.REQUIRED_FIELD_NAME);
            jsonGenerator.writeBoolean(true);
            jsonGenerator.writeFieldName(SwaggerConstants.ALLOW_MULTIPLE_FIELD_NAME);
            jsonGenerator.writeBoolean(false);
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeFieldName("summary");
        jsonGenerator.writeString(getDescription());

        jsonGenerator.writeFieldName(SwaggerConstants.RESPONSE_CLASS_FIELD_NAME);
        jsonGenerator.writeString(StringUtils.capitalize(NameUtils.camel(resource.getName())));

        jsonGenerator.writeEndObject();
    }

}
