/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.transform;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.NullPayload;

import java.util.List;

import org.raml.model.MimeType;

public class ApikitResponseTransformer extends AbstractMessageTransformer
{

    public static final String BEST_MATCH_REPRESENTATION = "_ApikitResponseTransformer_bestMatchRepresentation";
    public static final String CONTRACT_MIME_TYPES = "_ApikitResponseTransformer_contractMimeTypes";
    public static final String APIKIT_ROUTER_REQUEST = "_ApikitResponseTransformer_apikitRouterRequest";

    @Override
    public Object transformMessage(MuleMessage message, String encoding) throws TransformerException
    {
        if (message.getInvocationProperty(APIKIT_ROUTER_REQUEST) == null)
        {
            // request not originated from an apikit router
            return message;
        }
        String responseRepresentation = message.getInvocationProperty(BEST_MATCH_REPRESENTATION);
        List<MimeType> responseMimeTypes = message.getInvocationProperty(CONTRACT_MIME_TYPES);
        if (responseRepresentation == null)
        {
            message.setPayload(NullPayload.getInstance());
            return message;
        }
        return transformToExpectedContentType(message, responseRepresentation, responseMimeTypes);
    }

    public Object transformToExpectedContentType(MuleMessage message, String responseRepresentation, List<MimeType> responseMimeTypes) throws TransformerException
    {
        Object payload = message.getPayload();
        String msgMimeType = null;
        DataType<?> dataType = message.getDataType();
        if (dataType != null && dataType.getMimeType() != null && dataType.getEncoding() != null)
        {
            msgMimeType = dataType.getMimeType() + ";charset=" + dataType.getEncoding();
        }
        String msgContentType = message.getOutboundProperty("Content-Type");

        // user is in charge of setting content-type when using */*
        if ("*/*".equals(responseRepresentation))
        {
            if (msgContentType == null)
            {
                throw new ApikitRuntimeException("Content-Type must be set in the flow when declaring */* response type");
            }
            return payload;
        }

        if (payload instanceof NullPayload)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Response transformation not required. Message payload type is NullPayload");
            }
            return payload;
        }

        String msgAcceptedContentType = acceptedContentType(msgMimeType, msgContentType, responseMimeTypes);
        if (msgAcceptedContentType != null)
        {
            message.setOutboundProperty("Content-Type", msgAcceptedContentType);
            if (logger.isDebugEnabled())
            {
                logger.debug("Response transformation not required. Message payload type is " + msgAcceptedContentType);
            }
            return payload;
        }
        DataType sourceDataType = DataTypeFactory.create(message.getPayload().getClass(), msgMimeType);
        DataType resultDataType = DataTypeFactory.create(String.class, responseRepresentation);

        if (logger.isDebugEnabled())
        {
            logger.debug(String.format("Resolving transformer between [source=%s] and [result=%s]", sourceDataType, resultDataType));
        }

        Transformer transformer;
        try
        {
            transformer = TransformerCache.getTransformerCache(muleContext).get(new DataTypePair(sourceDataType, resultDataType));
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("Transformer resolved to [transformer=%s]", transformer));
            }
            Object newPayload = transformer.transform(message.getPayload());
            message.setOutboundProperty("Content-Type", responseRepresentation);
            return newPayload;
        }
        catch (Exception e)
        {
            throw new TransformerException(this, e);
        }

    }

    /**
     * checks if the current payload type is any of the accepted ones.
     *
     * @return null if it is not
     */
    private String acceptedContentType(String msgMimeType, String msgContentType, List<MimeType> responseMimeTypes)
    {
        for (MimeType responseMimeType : responseMimeTypes)
        {
            if (msgMimeType != null && msgMimeType.contains(responseMimeType.getType()))
            {
                return msgMimeType;
            }
            if (msgContentType != null && msgContentType.contains(responseMimeType.getType()))
            {
                return msgContentType;
            }
        }
        return null;
    }

}
