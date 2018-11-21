/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.mule.module.apikit.api.exception.InvalidUriParameterException;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.util.MultiMap;

import java.util.Map;

public class UriParametersValidator {

  MultiMap<String, String> uriParams;
  IAction action;
  ResolvedVariables resolvedVariables;

  public UriParametersValidator(IAction action, ResolvedVariables resolvedVariables) {
    this.action = action;
    this.resolvedVariables = resolvedVariables;
  }

  public MultiMap<String, String> validateAndAddDefaults(Map<String, String> uriParams)
      throws InvalidUriParameterException {
    this.uriParams = new MultiMap<>(uriParams);
    for (Map.Entry<String, IParameter> entry : action.getResolvedUriParameters().entrySet()) {
      String value = (String) resolvedVariables.get(entry.getKey());
      IParameter uriParameter = entry.getValue();
      if (!uriParameter.validate(value)) {
        String msg = String.format("Invalid value '%s' for uri parameter %s. %s",
                                   value, entry.getKey(), uriParameter.message(value));

        throw new InvalidUriParameterException(msg);
      }
    }

    for (String name : resolvedVariables.names()) {
      String value = String.valueOf(resolvedVariables.get(name));
      this.uriParams = AttributesHelper.addParam(this.uriParams, name, value);
    }
    return this.uriParams;
  }

}
