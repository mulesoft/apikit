/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.MuleEvent;
import org.mule.api.routing.filter.FilterUnacceptedException;
import org.mule.api.transformer.TransformerException;

import java.util.List;

import org.raml.model.MimeType;

public class HttpRestProxyRequest extends HttpRestRequest
{

    public HttpRestProxyRequest(MuleEvent event, AbstractConfiguration config)
    {
        super(event, config);
    }

    @Override
    protected MuleEvent processResponse(MuleEvent responseEvent, List<MimeType> responseMimeTypes, String responseRepresentation) throws TransformerException, FilterUnacceptedException
    {
        Proxy.copyProperties(responseEvent, Proxy.MULE_RESPONSE_HEADERS);
        return responseEvent;
    }
}
