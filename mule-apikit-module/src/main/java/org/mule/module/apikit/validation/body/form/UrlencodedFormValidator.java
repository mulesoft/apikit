/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.List;
import java.util.Map;

public class UrlencodedFormValidator implements FormValidatorStrategy<Map<String, String>> {

  Map<String, List<IParameter>> formParameters;

  public UrlencodedFormValidator(Map<String, List<IParameter>> formParameters) {
    this.formParameters = formParameters;
  }

  @Override
  public Map<String, String> validate(Map<String, String> payload) throws BadRequestException {

    for (String expectedKey : formParameters.keySet())
    {
      if (formParameters.get(expectedKey).size() != 1)
      {
        //do not perform validation when multi-type parameters are used
        continue;
      }

      IParameter expected = formParameters.get(expectedKey).get(0);

      Object actual = payload.get(expectedKey);

      if (actual == null && expected.isRequired())
      {
        throw ApikitErrorTypes.throwErrorType(new InvalidFormParameterException("Required form parameter " + expectedKey + " not specified"));
      }

      if (actual == null && expected.getDefaultValue() != null)
      {
        payload.put(expectedKey, expected.getDefaultValue());
      }

      if (actual != null && actual instanceof String)
      {
        if (!expected.validate((String) actual))
        {
          String msg = String.format("Invalid value '%s' for form parameter %s. %s",
              actual, expectedKey, expected.message((String) actual));
          throw ApikitErrorTypes.throwErrorType(new InvalidFormParameterException(msg));
        }
      }
    }

    return payload;
  }
}
