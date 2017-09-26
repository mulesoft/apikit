/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.validation;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.validation.AttributesValidator;
import org.mule.module.apikit.validation.BodyValidator;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.exception.DefaultMuleException;

public class RequestValidator {

  public static ValidRequest validate(ValidationConfig config, IResource resource, HttpRequestAttributes attributes,
                                      ResolvedVariables resolvedVariables, Object payload, String charset)
      throws DefaultMuleException, MuleRestException {

    return ValidRequest.builder()
        .withAttributes(AttributesValidator.validateAndAddDefaults(attributes, resource, resolvedVariables, config))
        .withBody(BodyValidator.validate(resource.getAction(attributes.getMethod().toLowerCase()), attributes, payload, config,
                                         charset))
        .build();

  }
}
