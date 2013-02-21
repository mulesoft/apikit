
package org.mule.module.apikit.rest.representation;

import com.google.common.net.MediaType;

public class DefaultRepresentation implements Representation
{

    protected MediaType mediaType;
    protected String schemaType;
    protected String schemaLocation;

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

}
