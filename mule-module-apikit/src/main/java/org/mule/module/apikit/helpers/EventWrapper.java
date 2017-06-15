/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import org.mule.extension.http.api.HttpHeaders;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.http.api.HttpConstants;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class EventWrapper
{
    private Event inputEvent;
    private Event.Builder outputBuilder;
    private HashMap<String, String> outboundHeaders = new HashMap<>();
    private String httpStatus;
    private String outboundHeadersMapName;
    private String httpStatusVarName;


    public EventWrapper (Event input, String outboundHeadersMapName, String httpStatusVarName)
    {
        inputEvent = input;
        outputBuilder = Event.builder(input);
        this.outboundHeadersMapName = outboundHeadersMapName;
        this.httpStatusVarName = httpStatusVarName;
        httpStatus = String.valueOf(HttpConstants.HttpStatus.OK.getStatusCode());
    }

    public void addOutboundProperties(Map<String, String> headers)
    {
        outboundHeaders.putAll(headers);
    }

    public Event build()
    {
        outputBuilder.addVariable(httpStatusVarName, httpStatus);
        outputBuilder.addVariable(outboundHeadersMapName, outboundHeaders);
        return outputBuilder.build();
    }


    public EventWrapper doClientRedirect()
    {
        httpStatus = String.valueOf(HttpConstants.HttpStatus.MOVED_PERMANENTLY.getStatusCode());
        String redirectLocation = getRedirectLocation(EventHelper.getHttpRequestAttributes(inputEvent));
        outboundHeaders.put(HttpHeaders.Names.LOCATION, redirectLocation);
        return this;
    }

    /**
     * Creates URL where the server must redirect the client
     * @param attributes
     * @return The redirect URL
     */
    private String getRedirectLocation(HttpRequestAttributes attributes)
    {
        String scheme = attributes.getScheme();
        String remoteAddress = attributes.getHeaders().get("host");
        String redirectLocation = scheme + "://" + remoteAddress + attributes.getRequestPath() + "/";
        String queryString = attributes.getQueryString();

        if (StringUtils.isNotEmpty(queryString))
        {
            redirectLocation += "?" + queryString;
        }

        return redirectLocation;
    }

    public EventWrapper setPayload(String payload, String mimeType)
    {
        outputBuilder.message(MessageHelper.setPayload(inputEvent.getMessage(), payload, mimeType));
        return this;
    }

    public EventWrapper setPayload(byte[] payload, MediaType mediaType)
    {
        outputBuilder.message(MessageHelper.setPayload(inputEvent.getMessage(), payload, mediaType));
        return this;
    }
}
