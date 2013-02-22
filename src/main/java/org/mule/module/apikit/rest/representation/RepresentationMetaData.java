package org.mule.module.apikit.rest.representation;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.RestRequest;

import com.google.common.net.MediaType;

public interface RepresentationMetaData
{

    MediaType getMediaType();
    String getSchemaType();
    String getSchemaLocation();

    Object fromRepresentation(Object payload);
    Object toRepresentation(MuleEvent muleEvent, RestRequest request);

}
