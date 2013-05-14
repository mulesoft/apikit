/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.protocol.http;

import static org.mule.module.apikit.rest.protocol.http.HttpStatusCode.CLIENT_ERROR_BAD_REQUEST;
import static org.mule.module.apikit.rest.protocol.http.HttpStatusCode.CLIENT_ERROR_METHOD_NOT_ALLOWED;
import static org.mule.module.apikit.rest.protocol.http.HttpStatusCode.CLIENT_ERROR_NOT_ACCEPTABLE;
import static org.mule.module.apikit.rest.protocol.http.HttpStatusCode.CLIENT_ERROR_NOT_FOUND;
import static org.mule.module.apikit.rest.protocol.http.HttpStatusCode.CLIENT_ERROR_UNAUTHORIZED;
import static org.mule.module.apikit.rest.protocol.http.HttpStatusCode.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE;
import static org.mule.module.apikit.rest.protocol.http.HttpStatusCode.SERVER_ERROR_INTERNAL;
import static org.mule.transport.http.HttpConnector.HTTP_STATUS_PROPERTY;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.apikit.UnauthorizedException;
import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.UnsupportedMediaTypeException;
import org.mule.module.apikit.rest.operation.OperationNotAllowedException;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.protocol.RestProtocolAdapter;
import org.mule.module.apikit.rest.protocol.UserDefinedStatusCodeException;
import org.mule.module.apikit.rest.resource.ResourceNotFoundException;
import org.mule.module.apikit.rest.validation.InvalidInputException;
import org.mule.transport.NullPayload;
import org.mule.util.StringUtils;

import com.google.common.net.MediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpRestProtocolAdapter implements RestProtocolAdapter
{
    private URI baseURI;
    private URI resourceURI;
    private RestOperationType operationType;
    private List<MediaType> acceptableResponseMediaTypes = Collections.emptyList();
    private MediaType requestMediaType;
    private Map<String, Object> queryParams;

    public HttpRestProtocolAdapter(MuleEvent event)
    {
        MuleMessage message = event.getMessage();
        this.baseURI = event.getMessageSourceURI();
        if (message.getInboundProperty("host") != null)
        {
            String hostHeader = message.getInboundProperty("host");
            if (hostHeader.indexOf(':') != -1)
            {
                String host = hostHeader.substring(0, hostHeader.indexOf(':'));
                int port = Integer.parseInt(hostHeader.substring(hostHeader.indexOf(':') + 1));
                try
                {
                    String requestPath;
                    requestPath = message.getInboundProperty("http.request.path");
                    this.resourceURI = new URI("http", null, host, port, requestPath, null, null);
                }
                catch (URISyntaxException e)
                {
                    throw new IllegalArgumentException("Cannot parse URI", e);
                }
            }
            else
            {
                try
                {
                    String requestPath;
                    requestPath = message.getInboundProperty("http.request.path");
                    this.resourceURI = new URI("http", null, (String) message.getInboundProperty("host"), 80,
                        requestPath, null, null);
                }
                catch (URISyntaxException e)
                {
                    throw new IllegalArgumentException("Cannot parse URI", e);
                }
            }
        }
        else
        {
            try
            {
                this.resourceURI = new URI("http", null, baseURI.getHost(), baseURI.getPort(),
                    (String) message.getInboundProperty("http.request.path"), null, null);
            }
            catch (URISyntaxException e)
            {
                throw new IllegalArgumentException("Cannot parse URI", e);
            }
        }
        String method = message.getInboundProperty("http.method");
        operationType = HttpMethod.fromHttpMethodString(method).toRestOperationType();

        if (!StringUtils.isBlank((String) message.getInboundProperty("accept")))
        {
            this.acceptableResponseMediaTypes = parseAcceptHeader((String) message.getInboundProperty("accept"));
        }

        if (!StringUtils.isBlank((String) message.getInboundProperty("content-type")))
        {
            this.requestMediaType = MediaType.parse((String) message.getInboundProperty("content-type"));
        }
        if (this.requestMediaType == null
            && !StringUtils.isBlank((String) message.getOutboundProperty("content-type")))
        {
            this.requestMediaType = MediaType.parse((String) message.getOutboundProperty("content-type"));
        }

        this.queryParams = message.getInboundProperty("http.query.params");
    }

    @Override
    public RestOperationType getOperationType()
    {
        return operationType;
    }

    @Override
    public URI getURI()
    {
        return resourceURI;
    }

    @Override
    public URI getBaseURI()
    {
        return baseURI;
    }

    @Override
    public Map<String, Object> getQueryParameters()
    {
        return queryParams;
    }

    private Set<String> actionTypesToHttpMethods(Set<RestOperationType> actionTypes)
    {
        Set<String> set = new HashSet<String>();
        for (RestOperationType type : actionTypes)
        {
            set.add(HttpMethod.fromRestOperationType(type).toString());
        }
        return set;
    }

    @Override
    public void handleException(RestException re, RestRequest request)
    {
        MuleMessage message = request.getMuleEvent().getMessage();
        if (re instanceof OperationNotAllowedException)
        {
            OperationNotAllowedException anse = (OperationNotAllowedException) re;
            message.setOutboundProperty("http.status", CLIENT_ERROR_METHOD_NOT_ALLOWED.getCode());
            message.setOutboundProperty("Allow",
                StringUtils.join(actionTypesToHttpMethods(anse.getResource().getAllowedOperationTypes()), " ,"));
            String uri = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.6";
            String detail = "The method specified in the Request-Line is not allowed for the resource identified by the Request-URI.";
            request.setErrorPayload(uri, "METHOD NOT ALLOWED", detail, CLIENT_ERROR_METHOD_NOT_ALLOWED.getCodeAsString());

        }
        else if (re instanceof ResourceNotFoundException)
        {
            message.setOutboundProperty("http.status", CLIENT_ERROR_NOT_FOUND.getCode());
            String uri = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.5";
            String detail = "The server has not found anything matching the Request-URI or the server does not wish " +
                            "to reveal exactly why the request has been refused, or no other response is applicable.";
            request.setErrorPayload(uri, "NOT FOUND", detail, CLIENT_ERROR_NOT_FOUND.getCodeAsString());
        }
        else if (re instanceof MediaTypeNotAcceptableException)
        {
            message.setOutboundProperty("http.status", CLIENT_ERROR_NOT_ACCEPTABLE.getCode());
            String uri = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.7";
            String detail = "The resource identified by the request is only capable of generating response entities " +
                            "whose content characteristics do not match the user's requirements (in Accept* headers).";
            request.setErrorPayload(uri, "NOT ACCEPTABLE", detail, CLIENT_ERROR_NOT_ACCEPTABLE.getCodeAsString());
        }
        else if (re instanceof UnsupportedMediaTypeException)
        {
            message.setOutboundProperty("http.status", CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode());
            String uri = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.16";
            String detail = "The server is refusing to service the request because the entity of the request is in a format not " +
                            "supported by the requested resource for the requested method.";
            request.setErrorPayload(uri, "UNSUPPORTED MEDIA TYPE", detail, CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCodeAsString());
        }
        else if (re instanceof UnauthorizedException)
        {
            message.setOutboundProperty("http.status", CLIENT_ERROR_UNAUTHORIZED.getCode());
            String uri = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.2";
            String detail = "The request requires user authentication.";
            request.setErrorPayload(uri, "UNAUTHORIZED", detail, CLIENT_ERROR_UNAUTHORIZED.getCodeAsString());
        }
        else if (re instanceof InvalidInputException)
        {
            message.setOutboundProperty("http.status", CLIENT_ERROR_BAD_REQUEST.getCode());
            String uri = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1";
            String detail = "The request could not be understood by the server due to malformed syntax.";
            request.setErrorPayload(uri, detail, "BAD REQUEST", CLIENT_ERROR_BAD_REQUEST.getCodeAsString());
        }
        else if (re instanceof UserDefinedStatusCodeException)
        {
            //status code and payload set by user
        }
        else
        {
            message.setOutboundProperty("http.status", SERVER_ERROR_INTERNAL.getCode());
            String uri = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.1";
            String title = re.getCause() != null ? re.getCause().getClass().getName() : re.getClass().getName();
            request.setErrorPayload(uri, title, re.getMessage(), SERVER_ERROR_INTERNAL.getCodeAsString());
        }
    }

    @Override
    public List<MediaType> getAcceptableResponseMediaTypes()
    {
        return acceptableResponseMediaTypes;
    }

    @Override
    public MediaType getRequestMediaType()
    {
        return requestMediaType;
    }

    private List<MediaType> parseAcceptHeader(String acceptHeader)
    {
        List<MediaType> mediaTypes = new LinkedList<MediaType>();
        String[] types = StringUtils.split(acceptHeader, ',');
        if (types != null)
        {
            for (String type : types)
            {
                MediaType mediaType = MediaType.parse(type.trim());
                if (!mediaType.parameters().containsKey("q"))
                {
                    mediaType = mediaType.withParameter("q", "1");
                }
                mediaTypes.add(mediaType);
            }
        }
        return mediaTypes;
    }

    @Override
    public void handleCreated(URI location, RestRequest request)
    {
        request.getMuleEvent()
            .getMessage()
            .setOutboundProperty(HTTP_STATUS_PROPERTY, HttpStatusCode.SUCCESS_CREATED.getCode());
        request.getMuleEvent().getMessage().setOutboundProperty("location", location.toString());

    }

    @Override
    public void handleNoContent(RestRequest request)
    {
        if (request.getMuleEvent() != null && request.getMuleEvent().getMessage() != null)
        {
            request.getMuleEvent().getMessage().setPayload(NullPayload.getInstance());
            request.getMuleEvent()
                .getMessage()
                .setOutboundProperty(HTTP_STATUS_PROPERTY, HttpStatusCode.SUCCESS_NO_CONTENT.getCode());
        }
    }

    @Override
    public void handleOK(RestRequest request)
    {
        request.getMuleEvent().getMessage().setOutboundProperty(HTTP_STATUS_PROPERTY, HttpStatusCode.SUCCESS_OK.getCode());
    }
    

    @Override
    public boolean isCustomStatusCodeSet(RestRequest request)
    {
        Object statusCode = request.getMuleEvent().getMessage().getOutboundProperty(HTTP_STATUS_PROPERTY);
        return statusCode != null;
    }

}
