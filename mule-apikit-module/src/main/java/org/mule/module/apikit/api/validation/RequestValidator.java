/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.validation;

import static org.mule.module.apikit.CharsetUtils.getEncoding;
import static org.mule.module.apikit.helpers.PayloadHelper.getPayloadAsByteArray;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import java.io.IOException;
import java.io.InputStream;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.CharsetUtils;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.exception.MethodNotAllowedException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.input.stream.RewindableInputStream;
import org.mule.module.apikit.validation.AttributesValidator;
import org.mule.module.apikit.validation.BodyValidator;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestValidator {

  private static Logger LOGGER = LoggerFactory.getLogger(RequestValidator.class);

  public static ValidRequest validate(ValidationConfig config, IResource resource, HttpRequestAttributes attributes,
                                      ResolvedVariables resolvedVariables, Object payload)
      throws MuleRestException {

    return validate(config, resource, attributes, resolvedVariables, payload, null, null);

  }

  public static ValidRequest validate(ValidationConfig config, IResource resource, HttpRequestAttributes attributes,
                                      ResolvedVariables resolvedVariables, Object payload,
                                      ErrorTypeRepository errorTypeRepository)
      throws MuleRestException {

    return validate(config, resource, attributes, resolvedVariables, payload, null, errorTypeRepository);

  }

  public static ValidRequest validate(ValidationConfig config, IResource resource, HttpRequestAttributes attributes,
                                      ResolvedVariables resolvedVariables, Object payload, String charset)
      throws MuleRestException {
    return validate(config, resource, attributes, resolvedVariables, payload, charset, null);
  }

  public static ValidRequest validate(ValidationConfig config, IResource resource, HttpRequestAttributes attributes,
                                      ResolvedVariables resolvedVariables, Object payload, String charset,
                                      ErrorTypeRepository errorTypeRepository)
      throws MuleRestException {

    final HttpRequestAttributes httpRequestAttributes;
    final ValidBody validBody;
    if (config.isDisableValidations()) {
      httpRequestAttributes = attributes;
      validBody = new ValidBody(payload);
    } else {
      if (resource == null)
        throw new MuleRuntimeException(createStaticMessage("Unexpected error. Resource cannot be null"));

      payload = makePayloadRepeatable(payload);

      if (charset == null) {
        final MultiMap<String, String> headers = attributes.getHeaders();
        try {
          charset = getCharset(headers, payload);
        } catch (IOException e) {
          throw new BadRequestException(e.getMessage());
        }
      }

      final String method = attributes.getMethod().toLowerCase();
      if (resource.getAction(method) == null) {
        final String version = resolvedVariables.get("version").toString();
        throw new MethodNotAllowedException(resource.getResolvedUri(version) + " : " + method);
      }

      httpRequestAttributes = AttributesValidator.validateAndAddDefaults(attributes, resource, resolvedVariables, config);
      validBody = BodyValidator.validate(resource.getAction(method), attributes, payload, config, charset, errorTypeRepository);
    }

    return ValidRequest.builder()
        .withAttributes(httpRequestAttributes)
        .withBody(validBody)
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
    } else if (payload instanceof InputStream && !(payload instanceof RewindableInputStream)) {
      return new RewindableInputStream((InputStream) payload);
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
