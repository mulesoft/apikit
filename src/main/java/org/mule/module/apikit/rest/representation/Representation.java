package org.mule.module.apikit.rest.representation;

import com.google.common.net.MediaType;

public interface Representation
{

    MediaType getMediaType();
    String getSchemaType();
    String getSchemaLocation();

}
