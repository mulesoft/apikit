/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.validation;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.CharsetUtils;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.input.stream.RewindableInputStream;
import org.mule.module.apikit.validation.AttributesValidator;
import org.mule.module.apikit.validation.BodyValidator;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static org.mule.module.apikit.CharsetUtils.getEncoding;
import static org.mule.module.apikit.helpers.PayloadHelper.getPayloadAsByteArray;

public class RequestValidator {

  private static Logger LOGGER = LoggerFactory.getLogger(RequestValidator.class);

  public static ValidRequest validate(ValidationConfig config, IResource resource, HttpRequestAttributes attributes,
                                      ResolvedVariables resolvedVariables, Object payload)
      throws MuleRestException {

    return validate(config, resource, attributes, resolvedVariables, payload, null);

  }

  public static ValidRequest validate(ValidationConfig config, IResource resource, HttpRequestAttributes attributes,
                                      ResolvedVariables resolvedVariables, Object payload, String charset)
      throws MuleRestException {

    payload = makePayloadRepeatable(payload);

    if (charset == null) {
      final MultiMap<String, String> headers = attributes.getHeaders();
      try {
        charset = getCharset(headers, payload);
      } catch (IOException e) {
        throw new BadRequestException(e.getMessage());
      }
    }

    return ValidRequest.builder()
        .withAttributes(AttributesValidator.validateAndAddDefaults(attributes, resource, resolvedVariables, config))
        .withBody(BodyValidator.validate(resource.getAction(attributes.getMethod().toLowerCase()), attributes, payload, config,
                                         charset))
        .build();

  }

  private static Object makePayloadRepeatable(Object payload) {
    if (payload instanceof TypedValue) {
      final TypedValue typedValue = (TypedValue) payload;
      final Object payloadValue = typedValue.getValue();
      if (payloadValue instanceof InputStream) {
        final RewindableInputStream rewindable = new RewindableInputStream((InputStream) payloadValue);
        return new TypedValue<>(rewindable, typedValue.getDataType());
      }
    }

    return payload;
  }

  private static String getCharset(MultiMap<String, String> headers, Object payload) throws IOException {
    String charset = getHeaderCharset(headers);

    if (charset == null) {
      if (payload instanceof TypedValue) {
        final TypedValue typedValue = (TypedValue) payload;
        final Object payloadValue = getPayloadAsByteArray(typedValue.getValue());
        charset = getEncoding(typedValue, payloadValue, LOGGER);
      } else {
        charset = getEncoding(getPayloadAsByteArray(payload), LOGGER);
      }
    }

    return charset;
  }

  private static String getHeaderCharset(MultiMap<String, String> headers) {
    return CharsetUtils.getCharset(AttributesHelper.getParamIgnoreCase(headers, "Content-Type"));
  }
}
