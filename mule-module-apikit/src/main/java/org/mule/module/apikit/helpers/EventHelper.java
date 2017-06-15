/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;


import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.validation.ValidRequest;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.Event;

import java.nio.charset.Charset;
import java.util.Optional;

public class EventHelper
{
    private EventHelper()
    {

    }

    public static Charset getEncoding(Event event)
    {
        return getEncoding(event.getMessage());
    }

    public static Charset getEncoding(Message message)
    {
        Optional<Charset> payloadEncoding = message.getPayload().getDataType().getMediaType().getCharset();
        return payloadEncoding.orElse(Charset.defaultCharset());// TODO Should we get default charset from mule?
    }

    public static Event.Builder regenerateEvent(Message message, Event.Builder builder, ValidRequest validRequest) {
        Message.Builder messageBuilder = Message.builder(message);
        messageBuilder.payload(validRequest.getBody().getPayload());
        messageBuilder.attributes(validRequest.getAttributes());

        return builder.message(messageBuilder.build());
    }

    public static HttpRequestAttributes getHttpRequestAttributes(Event event)
    {
        return ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());
    }

}
