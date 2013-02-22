
package org.mule.module.apikit.rest.representation;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.RestRequest;

import com.google.common.net.MediaType;

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
    public Object toRepresentation(MuleEvent event, RestRequest request)
    {
        return null;
    }

}
