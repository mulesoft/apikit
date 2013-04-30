/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.protocol;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.RestOperationType;

import com.google.common.net.MediaType;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface RestProtocolAdapter
{

    URI getURI();

    URI getBaseURI();

    RestOperationType getOperationType();

    List<MediaType> getAcceptableResponseMediaTypes();

    MediaType getRequestMediaType();

    Map<String, Object> getQueryParameters();

    void handleException(RestException re, RestRequest request);

    void handleCreated(URI location, RestRequest request);

    void handleNoContent(RestRequest request);

    void handleOK(RestRequest request);
}
