/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

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
