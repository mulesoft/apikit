/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.validation.body.form.transformation.DataWeaveDefaultsBuilder;
import org.mule.module.apikit.validation.body.form.transformation.DataWeaveTransformer;
import org.mule.module.apikit.validation.body.form.transformation.TextPlainPart;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class MultipartFormValidator implements FormValidatorStrategy<TypedValue> {

  protected static final Logger logger = LoggerFactory.getLogger(MultipartFormValidator.class);
  Map<String, List<IParameter>> formParameters;
  DataWeaveTransformer dataWeaveTransformer;

  public MultipartFormValidator(Map<String, List<IParameter>> formParameters, ExpressionManager expressionManager) {
    this.formParameters = formParameters;
    this.dataWeaveTransformer = new DataWeaveTransformer(expressionManager);

  }

  @Override
  public TypedValue validate(TypedValue originalPayload) throws InvalidFormParameterException {

    Map<String, String> actualParameters = dataWeaveTransformer.getMultiMapFromPayload(originalPayload);
    DataWeaveDefaultsBuilder defaultsBuilder = new DataWeaveDefaultsBuilder();

    for (String expectedKey : formParameters.keySet()) {
      if (formParameters.get(expectedKey).size() != 1) {
        //do not perform validation when multi-type parameters are used
        continue;
      }

      IParameter expected = formParameters.get(expectedKey).get(0);
      if (actualParameters.keySet().contains(expectedKey)) {
        String value = actualParameters.get(expectedKey);
        if (!expected.validate(value)) {
          throw new InvalidFormParameterException("Value " + value + " for parameter " + expectedKey + " is invalid");
        }
      } else {
        if (expected.getDefaultValue() != null) {
          defaultsBuilder.addPart(new TextPlainPart().setName(expectedKey).setValue(expected.getDefaultValue()));
        } else if (expected.isRequired()) {
          throw new InvalidFormParameterException("Required form parameter " + expectedKey + " not specified");
        }
      }
    }
    if (defaultsBuilder.areDefaultsToAdd()) {
      return dataWeaveTransformer.runDataWeaveScript(defaultsBuilder.build(), originalPayload.getDataType(), originalPayload);
    } else {
      return originalPayload;
    }
  }

}
