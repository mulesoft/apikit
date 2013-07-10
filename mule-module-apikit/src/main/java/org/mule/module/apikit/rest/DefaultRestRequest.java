/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.google.common.net.MediaType.XML_UTF_8;
import static org.apache.commons.lang.StringEscapeUtils.escapeXml;
import static org.mule.api.config.MuleProperties.CONTENT_TYPE_PROPERTY;
import static org.mule.module.apikit.rest.representation.RepresentationMetaData.MULE_RESPONSE_MEDIATYPE_PROPERTY;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.protocol.RestProtocolAdapter;
import org.mule.module.apikit.rest.protocol.RestProtocolAdapterFactory;
import org.mule.module.apikit.rest.representation.DefaultRepresentationMetaData;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;
import org.mule.module.apikit.rest.util.RestContentTypeParser;
import org.mule.transport.NullPayload;
import org.mule.util.StringUtils;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.google.common.net.MediaType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

public class DefaultRestRequest implements RestRequest
{

    protected MuleEvent event;
    protected RestProtocolAdapter protocolAdapter;
    protected Deque<String> pathStack;
    protected RestWebService restWebService;
    protected String relativeURI;

    public DefaultRestRequest(MuleEvent event, RestWebService restWebService)
    {
        this.event = event;
        this.protocolAdapter = createProtocolAdapter(event);
        this.restWebService = restWebService;
        relativeURI = calculateRelativeURI();
        initPathStack();
    }

    protected String calculateRelativeURI()
    {
        String baseURIPath = protocolAdapter.getBaseURI().getPath();
        String requestURIPath = protocolAdapter.getURI().getPath();
        return StringUtils.difference(baseURIPath, requestURIPath);
    }

    protected RestProtocolAdapter createProtocolAdapter(MuleEvent event)
    {
        return RestProtocolAdapterFactory.getInstance().getAdapterForEvent(event);
    }

    private void initPathStack()
    {
        Deque<String> pathStack = new ArrayDeque<String>(Arrays.asList(relativeURI.split("/")));
        if (!pathStack.isEmpty() && pathStack.peekFirst().isEmpty())
        {
            pathStack.removeFirst();
        }
        this.pathStack = pathStack;
    }

    @Override
    public MuleEvent getMuleEvent()
    {
        return event;
    }

    @Override
    public String getNextPathElement()
    {
        if (pathStack.isEmpty())
        {
            return "";
        }
        return pathStack.removeFirst();
    }
    
    @Override
    public String peekNextPathElement()
    {
        if (pathStack.isEmpty())
        {
            return "";
        }
        return pathStack.peekFirst();
    }

    @Override
    public boolean hasMorePathElements()
    {
        return !pathStack.isEmpty() && !pathStack.getFirst().equals("");
    }

    @Override
    public RestProtocolAdapter getProtocolAdaptor()
    {
        return protocolAdapter;
    }

    public RestWebService getService()
    {
        return restWebService;
    }

    @Override
    public void setErrorPayload(String uri, String title, String detail, String statusCode)
    {
        String responseMediaType = getMuleEvent().getMessage().getOutboundProperty(MULE_RESPONSE_MEDIATYPE_PROPERTY);
        if (responseMediaType == null)
        {
            MediaType bestMatch = RestContentTypeParser.bestMatch(getErrorRepresentations(), getProtocolAdaptor().getAcceptableResponseMediaTypes());
            responseMediaType = bestMatch != null ? bestMatch.withoutParameters().toString() : "no-content-type-set";
        }
        if (responseMediaType.equals(JSON_UTF_8.withoutParameters().toString()))
        {
            String response = "{\"describedBy\":\"" + escapeJson(uri) + "\"," +
            "\"title\":\"" + escapeJson(title) + "\"," +
            "\"detail\":\"" + escapeJson(detail) + "\"," +
            "\"httpStatus\":\"" + escapeJson(statusCode) + "\"}";
            getMuleEvent().getMessage().setPayload(response);
            getMuleEvent().getMessage().setOutboundProperty(CONTENT_TYPE_PROPERTY, responseMediaType);
        }
        else if (responseMediaType.equals(XML_UTF_8.withoutParameters().toString()))
        {
            String response = "<problem><describedBy>" + escapeXml(uri) + "</describedBy>" +
            "<title>" + escapeXml(title) + "</title>" +
            "<detail>" + escapeXml(detail) + "</detail>" +
            "<httpStatus>" + escapeXml(statusCode) + "</httpStatus></problem>";
            getMuleEvent().getMessage().setPayload(response);
            getMuleEvent().getMessage().setOutboundProperty(CONTENT_TYPE_PROPERTY, responseMediaType);
        }
        else
        {
            //TODO plain/text or nothing at all?
            getMuleEvent().getMessage().setPayload(NullPayload.getInstance());
        }
    }

    private String escapeJson(String text)
    {
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        if (text == null)
        {
            text = "-";
        }
        return String.valueOf(encoder.quoteAsString(text));
    }

    private Collection<RepresentationMetaData> getErrorRepresentations()
    {
        List<RepresentationMetaData> representations = new ArrayList<RepresentationMetaData>();
        representations.add(new DefaultRepresentationMetaData(JSON_UTF_8.withParameter("q", "0.6")));
        representations.add(new DefaultRepresentationMetaData(XML_UTF_8.withParameter("q", "0.5")));
        return representations;
    }
}
