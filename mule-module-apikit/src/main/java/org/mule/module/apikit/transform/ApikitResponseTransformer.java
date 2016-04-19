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
import org.mule.module.apikit.RestContentTypeParser;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.NullPayload;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.net.MediaType;

import java.util.Collection;
import java.util.List;

public class ApikitResponseTransformer extends AbstractMessageTransformer
{

    public static final String BEST_MATCH_REPRESENTATION = "_ApikitResponseTransformer_bestMatchRepresentation";
    public static final String CONTRACT_MIME_TYPES = "_ApikitResponseTransformer_contractMimeTypes";
    public static final String APIKIT_ROUTER_REQUEST = "_ApikitResponseTransformer_apikitRouterRequest";
    public static final String ACCEPT_HEADER = "_ApikitResponseTransformer_AcceptedHeaders";

    @Override
    public Object transformMessage(MuleMessage message, String encoding) throws TransformerException
    {
        if (message.getInvocationProperty(APIKIT_ROUTER_REQUEST) == null)
        {
            // request not originated from an apikit router
            return message;
        }
        String responseRepresentation = message.getInvocationProperty(BEST_MATCH_REPRESENTATION);
        List<IMimeType> responseMimeTypes = message.getInvocationProperty(CONTRACT_MIME_TYPES);
        String acceptedHeader = message.getInvocationProperty(ACCEPT_HEADER);
        if (responseRepresentation == null)
        {
            // clear response payload unless response status is manually set
            if (message.getOutboundProperty("http.status") == null)
            {
                message.setPayload(NullPayload.getInstance());
            }
            return message;
        }
        return transformToExpectedContentType(message, responseRepresentation, responseMimeTypes, acceptedHeader);
    }

    public Object transformToExpectedContentType(MuleMessage message, String responseRepresentation, List<IMimeType> responseMimeTypes,
                                                 String acceptedHeader) throws TransformerException
    {
        Object payload = message.getPayload();
        String msgMimeType = null;
        DataType<?> dataType = message.getDataType();
        if (dataType != null && dataType.getMimeType() != null)
        {
            msgMimeType = dataType.getMimeType() + ";charset=" + message.getEncoding();
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

        Collection<IMimeType> conjunctionTypes = getBestMatchMediaTypes(responseMimeTypes, acceptedHeader);
        String msgAcceptedContentType = acceptedContentType(msgMimeType, msgContentType, conjunctionTypes);
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

    private Collection<IMimeType> getBestMatchMediaTypes(List<IMimeType> responseMimeTypes, String acceptedHeader)
    {
        if(acceptedHeader.contains("*/*"))
        {
            return responseMimeTypes;
        }
        final Collection<String> acceptedTypes = transformAcceptedTypes(acceptedHeader);

        return filterAccepted(responseMimeTypes, acceptedTypes);
    }

    private Collection<IMimeType> filterAccepted(List<IMimeType> responseMimeTypes, final Collection<String> acceptedTypes)
    {
        return Collections2.filter(
                responseMimeTypes, new Predicate<IMimeType>()
                {
                    @Override
                    public boolean apply(IMimeType m)
                    {
                        return acceptedTypes.contains(m.getType());
                    }
                }
        );
    }

    private Collection<String> transformAcceptedTypes(String acceptedHeader)
    {
        List<MediaType> acceptedMediaTypes = RestContentTypeParser.parseMediaTypes(acceptedHeader);

        return Collections2.transform(acceptedMediaTypes, new Function<MediaType, String>()
        {
            @Override
            public String apply(MediaType mediaType)
            {
                return mediaType.type() + "/" + mediaType.subtype();
            }
        });
    }

    /**
     * checks if the current payload type is any of the accepted ones.
     *
     * @return null if it is not
     */
    private String acceptedContentType(String msgMimeType, String msgContentType, Collection<IMimeType> conjunctionTypes)
    {
        for (IMimeType acceptedMediaType : conjunctionTypes)
        {
            if(msgMimeType != null && msgMimeType.contains(acceptedMediaType.getType()))
            {
                return msgMimeType;
            }
        }
        for (IMimeType acceptedMediaType : conjunctionTypes)
        {
            if(msgContentType != null && msgContentType.contains(acceptedMediaType.getType()))
            {
                return msgContentType;
            }
        }
        return null;
    }

}
