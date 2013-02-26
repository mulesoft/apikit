
package org.mule.module.apikit.rest.representation;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.MessageFactory;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transformer.types.SimpleDataType;

import com.google.common.net.MediaType;

import java.util.List;

public class DefaultRepresentationMetaData implements RepresentationMetaData
{

    protected MediaType mediaType;
    protected String schemaType;
    protected String schemaLocation;

    public DefaultRepresentationMetaData()
    {
    }

    public DefaultRepresentationMetaData(MediaType mediaType)
    {
        this.mediaType = mediaType;
    }

    @Override
    public MediaType getMediaType()
    {
        return mediaType;
    }

    @Override
    public String getSchemaType()
    {
        return schemaType;
    }

    @Override
    public String getSchemaLocation()
    {
        return schemaLocation;
    }

    public void setMediaType(MediaType mediaType)
    {
        this.mediaType = mediaType;
    };

    public void setSchemaType(String schemaType)
    {
        this.schemaType = schemaType;
    };

    public void setSchemaLocation(String schemaLocation)
    {
        this.schemaLocation = schemaLocation;
    };

    @Override
    public Object fromRepresentation(Object payload)
    {
        return null;
    }

    @Override
    public Object toRepresentation(MuleEvent event, RestRequest request) throws MuleException
    {
        DataType sourceDataType = DataTypeFactory.create(event.getMessage().getPayload().getClass());
        DataType resultDataType = new SimpleDataType<String>(String.class, mediaType.withoutParameters().toString());
        //Transformer transformer = event.getMuleContext().getRegistry().lookupTransformer(sourceDataType, resultDataType);
        List<Transformer> transformers = event.getMuleContext().getRegistry().lookupTransformers(sourceDataType, resultDataType);
        if (transformers == null || transformers.isEmpty())
        {
            throw new TransformerException(MessageFactory.createStaticMessage(String.format(
                    "No transformer found for the following types: %s -> %s\n", sourceDataType, resultDataType)));
        }
        Object payload = transformers.get(0).process(event).getMessage().getPayload();
        return payload;
    }

}
