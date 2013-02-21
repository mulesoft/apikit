package org.mule.module.apikit.rest.representation;

import com.google.common.net.MediaType;

public interface RepresentationType
{

    MediaType getMediaType();
    String getSchemaType();
    String getSchemaLocation();

}
