/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.exception.InvalidFormParameterException;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.message.MultiPartPayload;
import org.mule.runtime.core.message.PartAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipartFormValidator implements FormValidatorStrategy<MultiPartPayload> {
  protected final Logger logger = LoggerFactory.getLogger(MultipartFormValidator.class);
  Map<String, List<IParameter>> formParameters;

  public MultipartFormValidator(Map<String, List<IParameter>> formParameters) {
    this.formParameters = formParameters;
  }

  @Override
  public MultiPartPayload validate(MultiPartPayload payload) throws BadRequestException {

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
        data = payload.getPart(expectedKey);
      }
      catch (NoSuchElementException e)
      {
        data = null;
      }

      if (data == null && expected.isRequired())
      {
        //perform only 'required' validation to avoid consuming the stream
        throw ApikitErrorTypes.throwErrorTypeNew(new InvalidFormParameterException("Required form parameter " + expectedKey + " not specified"));
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
          payload.getParts().add(part1);
          //((DefaultMuleMessage) requestEvent.getMessage()).addInboundAttachment(expectedKey, defaultDataHandler);
        }
        catch (Exception e)
        {
          logger.warn("Cannot set default part " + expectedKey, e);
        }
      }
    }
    return payload;
  }

}
