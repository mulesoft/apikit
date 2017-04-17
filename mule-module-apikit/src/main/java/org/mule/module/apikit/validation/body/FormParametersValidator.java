/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.AttributesHelper;
import org.mule.module.apikit.EventHelper;
import org.mule.module.apikit.MessageHelper;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.exception.InvalidFormParameterException;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.raml.implv2.v10.model.MimeTypeImpl;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.message.MultiPartPayload;
import org.mule.runtime.core.message.PartAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.raml.v2.api.model.common.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormParametersValidator
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    IMimeType actionMimeType;
    boolean isParserV2;

    public FormParametersValidator(IMimeType actionMimeType, boolean isParserV2)
    {
        this.actionMimeType = actionMimeType;
        this.isParserV2 = isParserV2;
    }

    public Message validate(Message message) throws MuleRestException
    {
        String requestMimeType = AttributesHelper.getMediaType((HttpRequestAttributes)message.getAttributes());
        if (actionMimeType.getFormParameters() != null && requestMimeType.contains("multipart/form-data"))
        {
            validateMultipartForm(message, actionMimeType.getFormParameters());
            return message;
        }
        else if (actionMimeType.getFormParameters() != null && requestMimeType.contains("application/x-www-form-urlencoded"))
        {
            if (isParserV2)
            {
                validateUrlencodedFormV2(message, actionMimeType);
                return message;
            }
            else
            {
                return validateUrlencodedForm(message, actionMimeType.getFormParameters());
            }
        }
        return message;
    }

    @SuppressWarnings("unchecked")
    private Message validateUrlencodedForm(Message message, Map<String, List<IParameter>> formParameters) throws BadRequestException
    {
        Map<String, String> paramMap;
        paramMap = (Map<String, String>) message.getPayload().getValue();
        for (String expectedKey : formParameters.keySet())
        {
            if (formParameters.get(expectedKey).size() != 1)
            {
                //do not perform validation when multi-type parameters are used
                continue;
            }

            IParameter expected = formParameters.get(expectedKey).get(0);
            Object actual = paramMap.get(expectedKey);
            if (actual == null && expected.isRequired())
            {
                throw new InvalidFormParameterException("Required form parameter " + expectedKey + " not specified");
            }
            if (actual == null && expected.getDefaultValue() != null)
            {
                paramMap.put(expectedKey, expected.getDefaultValue());
            }
            if (actual != null && actual instanceof String)
            {
                if (!expected.validate((String) actual))
                {
                    String msg = String.format("Invalid value '%s' for form parameter %s. %s",
                                               actual, expectedKey, expected.message((String) actual));
                    throw new InvalidFormParameterException(msg);
                }
            }
        }
        //TODO SETPAYLOAD SHOULD USE A MIMETYPE
        return MessageHelper.setPayload(message, paramMap);
    }

    private void validateUrlencodedFormV2(Message message, IMimeType actionMimeType) throws MuleRestException
    {
        if (!(actionMimeType instanceof MimeTypeImpl))
        {
            // validate only raml 1.0
            return;
        }
        String jsonText;
        try
        {
            Map<String, String> payload = (Map<String, String>) message.getPayload().getValue();
            jsonText = new ObjectMapper().writeValueAsString(payload);
        }
        catch (Exception e)
        {
            logger.warn("Cannot validate url-encoded form", e);
            return;
        }

        List<ValidationResult> validationResult = ((MimeTypeImpl) actionMimeType).validate(jsonText);
        if (validationResult.size() > 0)
        {
            String resultString =  "";
            for (ValidationResult result : validationResult)
            {
                resultString += result.getMessage() + "\n";
            }
            throw new InvalidFormParameterException(resultString);
        }
    }


    //TODO FIX THIS METHOD
    private void validateMultipartForm(Message message, Map<String, List<IParameter>> formParameters) throws BadRequestException
    {
        for (String expectedKey : formParameters.keySet())
        {
            if (formParameters.get(expectedKey).size() != 1)
            {
                //do not perform validation when multi-type parameters are used
                continue;
            }
            IParameter expected = formParameters.get(expectedKey).get(0);
            Message data;
            try
            {
                data = ((MultiPartPayload) message.getPayload().getValue()).getPart(expectedKey);
            }
            catch (NoSuchElementException e)
            {
                data = null;
            }
            if (data == null && expected.isRequired())
            {
                //perform only 'required' validation to avoid consuming the stream
                throw new InvalidFormParameterException("Required form parameter " + expectedKey + " not specified");
            }
            if (data == null && expected.getDefaultValue() != null)
            {
                //TODO create message for default values

                //                DataHandler defaultDataHandler = new DataHandler(new StringDataSource(expected.getDefaultValue(), expectedKey));
                PartAttributes part1Attributes = new PartAttributes(expectedKey,
                                                                    null,
                                                                    expected.getDefaultValue().length(),
                                                                    Collections.emptyMap());
                Message part1 = Message.builder().payload(expected.getDefaultValue()).attributes(part1Attributes).build();


                try
                {
                    ((MultiPartPayload)message.getPayload().getValue()).getParts().add(part1);
                    //((DefaultMuleMessage) requestEvent.getMessage()).addInboundAttachment(expectedKey, defaultDataHandler);
                }
                catch (Exception e)
                {
                    logger.warn("Cannot set default part " + expectedKey, e);
                }
            }
        }
    }

}
