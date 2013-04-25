package org.mule.module.apikit.rest.representation;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.validation.InvalidInputException;
import org.mule.module.apikit.rest.validation.InvalidSchemaTypeException;

import com.google.common.net.MediaType;

public interface RepresentationMetaData
{

    MediaType getMediaType();
    SchemaType getSchemaType();
    String getSchemaLocation();

    Object fromRepresentation(Object payload);
    Object toRepresentation(MuleEvent muleEvent, RestRequest request) throws MuleException;

    void validate(RestRequest request) throws InvalidInputException, InvalidSchemaTypeException;
}
