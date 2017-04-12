/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;


import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.Event;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EventHelper
{

    public static Charset getEncoding(Event event)
    {
        return getEncoding(event.getMessage());
    }

    public static Charset getEncoding(Message message)
    {
        Optional<Charset> payloadEncoding = message.getPayload().getDataType().getMediaType().getCharset();
        return payloadEncoding.orElse(Charset.defaultCharset());// TODO Should we get default charset from mule?
    }

    public static Event regenerateEvent(Event event, HttpRequestAttributes newAttributes)
    {
        Event.Builder builder = Event.builder(event);
        Message.Builder messageBuilder = Message.builder(event.getMessage());
        messageBuilder.attributes(newAttributes);
        return builder.message(messageBuilder.build()).build();
    }

    public static Event regenerateEvent(Event event, Message newMessage)
    {
        Event.Builder builder = Event.builder(event);
        return builder.message(newMessage).build();
    }

    public static Event addVariable(Event event, String key, Object value)
    {
        Event.Builder builder = Event.builder(event);
        builder.addVariable(key, value);
        return builder.build();
    }

    public static Object getVariable(Event event, String key)
    {
        Object value;
        try
        {
            value = event.getVariable(key);
        }
        catch (Exception e)
        {
            return null;
        }
        return value;
    }

    public static Event addOutboundProperty(Event event, String outboundHeadersMapName, String key, String value)
    {
        Map<String, String> header = new HashMap<>();
        header.put(key, value);
        return addOutboundProperties(event, outboundHeadersMapName, header);
    }

    public static Event addOutboundProperties(Event event, String outboundHeadersMapName, Map<String, String> headers)
    {
        Event.Builder builder = Event.builder(event);
        Map<String, String> outboundHeaders = new HashMap<>();
        if (event.getVariable(outboundHeadersMapName) != null)
        {
            outboundHeaders = new HashMap<>((Map<String, String>) event.getVariable(outboundHeadersMapName).getValue());
        }
        outboundHeaders.putAll(headers);
        builder.addVariable(outboundHeadersMapName, outboundHeaders);
        return builder.build();
    }

    //public static Event setPayload(Event event, Object payload, String primaryType, String secondaryType)
    //{
    //    Event.Builder builder = Event.builder(event);
    //    InternalMessage.Builder messageBuilder = InternalMessage.builder(event.getMessage());
    //    messageBuilder.payload(payload);
    //    messageBuilder.mediaType(MediaType.create(primaryType, secondaryType));
    //    return builder.message(messageBuilder.build()).build();
    //}
    //
    //public static Event setPayload(Event event, Object payload, String mimetype)
    //{
    //    Event.Builder builder = Event.builder(event);
    //    InternalMessage.Builder messageBuilder = InternalMessage.builder(event.getMessage());
    //    messageBuilder.payload(payload);
    //    messageBuilder.mediaType(MediaType.parse(mimetype));
    //    return builder.message(messageBuilder.build()).build();
    //}
    //
    //
    //
    //public static Event setPayload(Event event, Object payload, MediaType mediatype)
    //{
    //    Event.Builder builder = Event.builder(event);
    //    Message newMessage = MessageHelper.setPayload(event.getMessage(), payload, mediatype);
    //    return builder.message(newMessage).build();
    //}
    //
    //public static Event setPayload(Event event, Object payload)
    //{
    //    Event.Builder builder = Event.builder(event);
    //    InternalMessage.Builder messageBuilder = InternalMessage.builder(event.getMessage());
    //    messageBuilder.payload(payload);
    //    return builder.message(messageBuilder.build()).build();
    //}
    //
    //public static Event setNullPayload(Event event)
    //{
    //    Event.Builder builder = Event.builder(event);
    //    InternalMessage.Builder messageBuilder = InternalMessage.builder(event.getMessage());
    //    messageBuilder.nullPayload();
    //    return builder.message(messageBuilder.build()).build();
    //}


    //public static Event addQueryParameters(Event event, Map<String, String> queryParams)
    //{
    //    Event.Builder builder = Event.builder(event);
    //    InternalMessage.Builder messageBuilder = InternalMessage.builder(event.getMessage());
    //
    //    HttpRequestAttributes oldAttributes = ((HttpRequestAttributes) event.getMessage().getAttributes());
    //    Map<String, LinkedList<String>> mapQueryParams = new HashMap<>();
    //    queryParams.putAll(oldAttributes.getQueryParams());
    //    for (Map.Entry<String, String> entry : queryParams.entrySet())
    //    {
    //        LinkedList<String> list = new LinkedList<>();
    //        list.add(entry.getValue());
    //        mapQueryParams.put(entry.getKey(), list);
    //    }
    //    ParameterMap inboundQueryParams = new ParameterMap(mapQueryParams);
    //    HttpRequestAttributes newAttributes = new HttpRequestAttributes(oldAttributes.getHeaders(), oldAttributes.getListenerPath(), oldAttributes.getRelativePath(), oldAttributes.getVersion(), oldAttributes.getScheme(), oldAttributes.getMethod(), oldAttributes.getRequestPath(), oldAttributes.getRequestUri(), oldAttributes.getQueryString(), inboundQueryParams, oldAttributes.getUriParams(), oldAttributes.getRemoteAddress(), oldAttributes.getClientCertificate());
    //
    //    messageBuilder.attributes(newAttributes);
    //    return builder.message(messageBuilder.build()).build();
    //}


}
