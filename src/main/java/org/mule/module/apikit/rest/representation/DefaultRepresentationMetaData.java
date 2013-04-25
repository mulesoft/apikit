
package org.mule.module.apikit.rest.representation;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.Transformer;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.transform.DataTypePair;
import org.mule.module.apikit.rest.transform.TransformerCache;
import org.mule.module.apikit.rest.validation.InvalidInputException;
import org.mule.module.apikit.rest.validation.InvalidSchemaTypeException;
import org.mule.module.apikit.rest.validation.RestSchemaValidator;
import org.mule.module.apikit.rest.validation.RestSchemaValidatorFactory;
import org.mule.transformer.types.DataTypeFactory;

import com.google.common.net.MediaType;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRepresentationMetaData implements RepresentationMetaData
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected MediaType mediaType;
    protected SchemaType schemaType;
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
    public SchemaType getSchemaType()
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
    }

    public void setSchemaType(SchemaType schemaType)
    {
        this.schemaType = schemaType;
    }

    public void setSchemaLocation(String schemaLocation)
    {
        this.schemaLocation = schemaLocation;
    }

    @Override
    public Object fromRepresentation(Object payload)
    {
        return null;
    }

    @Override
    public Object toRepresentation(MuleEvent muleEvent, RestRequest request) throws MuleException
    {
        DataType sourceDataType = DataTypeFactory.create(muleEvent.getMessage().getPayload().getClass());
        DataType resultDataType = DataTypeFactory.create(String.class, mediaType.withoutParameters().toString());

        if (logger.isDebugEnabled())
        {
            logger.debug(String.format("Resolving transformer between [source=%s] and [result=%s]", sourceDataType, resultDataType));
        }

        Transformer transformer;
        try
        {
            transformer = TransformerCache.getTransformerCache(muleEvent.getMuleContext()).get(new DataTypePair(sourceDataType, resultDataType));
        }
        catch (ExecutionException e)
        {
            throw new DefaultMuleException(e.getCause());
        }

        if (logger.isDebugEnabled())
        {
            logger.debug(String.format("Transformer resolved to [transformer=%s]", transformer));
        }

        Object payload = transformer.transform(muleEvent.getMessage().getPayload());
        muleEvent.getMessage().setOutboundProperty("Content-Type", mediaType.withoutParameters().toString());

        return payload;
    }

    @Override
    public void validate(RestRequest request) throws InvalidInputException, InvalidSchemaTypeException
    {
        if (schemaType == null || schemaLocation == null)
        {
            return; //no validation needed
        }
        MuleEvent event = request.getMuleEvent();
        RestSchemaValidator validator = RestSchemaValidatorFactory.getInstance().createValidator(schemaType, event.getMuleContext());
        validator.validate(schemaLocation, event);

    }

}
