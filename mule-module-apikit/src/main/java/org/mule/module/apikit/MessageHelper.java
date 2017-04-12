/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.service.http.api.domain.ParameterMap;

public class MessageHelper
{
    public static String getHeaderIgnoreCase(Message message, String name)
    {
        ParameterMap headers = ((HttpRequestAttributes) message.getAttributes().getValue()).getHeaders();
        return getParamIgnoreCase(headers, name);
    }

    public static String getParamIgnoreCase(ParameterMap parameters, String name)
    {
        for (String header : parameters.keySet())
        {
            if (header.equalsIgnoreCase(name.toLowerCase()))
            {
                return parameters.get(header);
            }
        }
        return null;
    }

    public static String getMediaType(Message message)
    {
        String contentType = getHeaderIgnoreCase(message, HeaderNames.CONTENT_TYPE);
        return contentType != null ? contentType.split(";")[0] : null;
    }

    public static Message setPayload(Message message, Object payload)
    {
        MediaType mediaType = null;
        return setPayload(message, payload, mediaType);
    }

    public static Message setPayload(Message message, Object payload, String mimetype)
    {
        return setPayload(message, payload, MediaType.parse(mimetype));
    }

    public static Message setPayload(Message message, Object payload, MediaType mediatype)
    {
        Message.Builder messageBuilder = Message.builder(message);
        messageBuilder.payload(payload);
        if (mediatype != null)
        {
            messageBuilder.mediaType(mediatype);
        }
        return messageBuilder.build();
    }
}
