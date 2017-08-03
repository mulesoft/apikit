package org.mule.module.metadata.model;

import org.mule.metadata.api.model.MetadataType;
import org.mule.module.metadata.MetadataFactory;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.List;
import java.util.Map;

public class Payload
{
    private static final String MIME_APPLICATION_JSON = "application/json";
    private static final String MIME_APPLICATION_XML = "application/xml";
    private static final String MIME_MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String MIME_APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";

    private Payload() {}

    public static MetadataType metadata(IMimeType body) {

        if (body == null) {
            return MetadataFactory.defaultMetadata();
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
                return MetadataFactory.defaultMetadata();
        }

    }

    private static MetadataType formMetadata(Map<String, List<IParameter>> formParameters)
    {
        return MetadataFactory.fromFormMetadata(formParameters);
    }

    private static MetadataType applicationXmlMetadata(String schema, String example)
    {
        if (schema != null)
        {
            return MetadataFactory.fromXSDSchema(schema);

        }
        else if (example != null)
        {
            return MetadataFactory.fromXMLExample(example);
        }

        return MetadataFactory.defaultMetadata();
    }

    private static MetadataType applicationJsonMetadata(String schema, String example)
    {
        if (schema != null)
        {
            return MetadataFactory.fromJsonSchema(schema);
        }
        else if (example != null)
        {
            return MetadataFactory.fromJsonExample(example);
        }

        return MetadataFactory.defaultMetadata();
    }

}
