/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.validation;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.MethodNotAllowedException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.validation.AttributesValidator;
import org.mule.module.apikit.validation.BodyValidator;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.exception.MuleRuntimeException;

import static org.mule.apikit.common.CommonUtils.cast;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

public class RequestValidator {

  public static ValidRequest validate(ValidationConfig config, IResource resource, HttpRequestAttributes attributes,
                                      ResolvedVariables resolvedVariables, Object payload, String charset)
      throws MuleRestException {

    final HttpRequestAttributes httpRequestAttributes;
    final ValidBody validBody;
    if (config.isDisableValidations()) {
      httpRequestAttributes = attributes;
      validBody = new ValidBody(payload);
    } else {
      if (resource == null)
        throw new MuleRuntimeException(createStaticMessage("Unexpected error. Resource cannot be null"));

      final String method = attributes.getMethod().toLowerCase();
      if (resource.getAction(method) == null) {
        final String version = cast(resolvedVariables.get("version"));
        throw new MethodNotAllowedException(resource.getResolvedUri(version) + " : " + method);
      }

      httpRequestAttributes = AttributesValidator.validateAndAddDefaults(attributes, resource, resolvedVariables, config);
      validBody = BodyValidator.validate(resource.getAction(method), attributes, payload, config, charset);
    }

    return ValidRequest.builder()
        .withAttributes(httpRequestAttributes)
        .withBody(validBody)
        .build();

  }
}
