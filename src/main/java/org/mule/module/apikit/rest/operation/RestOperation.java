/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.rest.operation;

import org.mule.module.apikit.api.WebServiceOperation;
import org.mule.module.apikit.rest.RestRequestHandler;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;

import java.io.IOException;
import java.util.Collection;

import org.codehaus.jackson.JsonGenerator;

public interface RestOperation extends RestRequestHandler, WebServiceOperation
{

    RestOperationType getType();

    Collection<RepresentationMetaData> getRepresentations();

    void appendSwaggerDescriptor(JsonGenerator jsonGenerator) throws IOException;
}
