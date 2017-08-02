package org.mule.module.metadata;

import org.mule.metadata.api.model.MetadataType;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.List;
import java.util.Map;

public class Body
{
    private static final String MIME_APPLICATION_JSON = "application/json";
    private static final String MIME_APPLICATION_XML = "application/xml";
    private static final String MIME_MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String MIME_APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";

    public static MetadataType payloadMetadata(IMimeType body) {

        if (body == null) {
            return MetadataUtils.defaultMetadata();
        }

        String type = body.getType();
        String schema = body.getSchema();
        String example = body.getExample();

        switch (type)
        {
            case MIME_APPLICATION_JSON:
                return applicationJsonMetadata(schema, example);
            case MIME_APPLICATION_XML:
                return applicationXmlMetadata(schema, example);
            case MIME_APPLICATION_URL_ENCODED:
                return formMetadata(body.getFormParameters());
            case MIME_MULTIPART_FORM_DATA:
                return formMetadata(body.getFormParameters());
            default:
                return MetadataUtils.defaultMetadata();
        }

    }

    private static MetadataType formMetadata(Map<String, List<IParameter>> formParameters)
    {
        return MetadataUtils.fromFormMetadata(formParameters);
    }

    private static MetadataType applicationXmlMetadata(String schema, String example)
    {
        if (schema != null)
        {
            return MetadataUtils.fromXSDSchema(schema);

        }
        else if (example != null)
        {
            return MetadataUtils.fromXMLExample(example);
        }

        return MetadataUtils.defaultMetadata();
    }

    private static MetadataType applicationJsonMetadata(String schema, String example)
    {
        if (schema != null)
        {
            return MetadataUtils.fromJsonSchema(schema);
        }
        else if (example != null)
        {
            return MetadataUtils.fromJsonExample(example);
        }

        return MetadataUtils.defaultMetadata();
    }

}
