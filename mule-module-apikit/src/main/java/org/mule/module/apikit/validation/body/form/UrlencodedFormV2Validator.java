/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.exception.InvalidFormParameterException;
import org.mule.raml.implv2.v10.model.MimeTypeImpl;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.raml.v2.api.model.common.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlencodedFormV2Validator implements FormValidatorStrategy<Map<String, String>> {
  protected final Logger logger = LoggerFactory.getLogger(UrlencodedFormV2Validator.class);
  Map<String, List<IParameter>> formParameters;
  IMimeType actionMimeType;

  public UrlencodedFormV2Validator(IMimeType actionMimeType) {
    this.formParameters = actionMimeType.getFormParameters();
    this.actionMimeType = actionMimeType;
  }

  @Override
  public Map<String, String> validate(Map<String, String> payload) throws BadRequestException {

    if (!(actionMimeType instanceof MimeTypeImpl))
    {
      // validate only raml 1.0
      return payload;
    }

    String jsonText;

    try
    {
      jsonText = new ObjectMapper().writeValueAsString(payload);
    }
    catch (Exception e)
    {
      logger.warn("Cannot validate url-encoded form", e);
      return payload;
    }

    List<ValidationResult> validationResult = ((MimeTypeImpl) actionMimeType).validate(jsonText);
    if (validationResult.size() > 0)
    {
      String resultString =  "";
      for (ValidationResult result : validationResult)
      {
        resultString += result.getMessage() + "\n";
      }
      throw ApikitErrorTypes.throwErrorTypeNew(new InvalidFormParameterException(resultString));
    }

    return payload;
  }
}
