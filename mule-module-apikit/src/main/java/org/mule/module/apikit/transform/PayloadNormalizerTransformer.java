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
import org.mule.config.i18n.MessageFactory;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transformer.types.DataTypeFactory;

public class PayloadNormalizerTransformer extends AbstractMessageTransformer
{

    @Override
    public Object transformMessage(MuleMessage message, String encoding) throws TransformerException
    {
        DataType sourceDataType = DataTypeFactory.create(message.getPayload().getClass(), (String) message.getInboundProperty("content-type"));
        DataType resultDataType = getReturnDataType();

        Transformer transformer;
        try
        {
            transformer = TransformerCache.getTransformerCache(muleContext).get(new DataTypePair(sourceDataType, resultDataType));
        }
        catch (Exception e)
        {
            throw new TransformerException(MessageFactory.createStaticMessage(e.getMessage()), e);
        }

        return transformer.transform(message.getPayload());
    }
}
