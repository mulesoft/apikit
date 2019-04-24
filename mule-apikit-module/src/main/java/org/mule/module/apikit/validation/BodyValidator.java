/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import static java.lang.String.format;
import static org.mule.module.apikit.helpers.PayloadHelper.getPayloadAsString;

import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.validation.ApiKitJsonSchema;
import org.mule.module.apikit.api.validation.ValidBody;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.module.apikit.validation.body.form.FormParametersValidator;
import org.mule.module.apikit.validation.body.form.MultipartFormValidator;
import org.mule.module.apikit.validation.body.form.UrlencodedFormV1Validator;
import org.mule.module.apikit.validation.body.form.UrlencodedFormV2Validator;
import org.mule.module.apikit.validation.body.schema.IRestSchemaValidatorStrategy;
import org.mule.module.apikit.validation.body.schema.v1.RestJsonSchemaValidator;
import org.mule.module.apikit.validation.body.schema.v1.RestXmlSchemaValidator;
import org.mule.module.apikit.validation.body.schema.v1.cache.SchemaCacheUtils;
import org.mule.module.apikit.validation.body.schema.v2.RestSchemaV2Validator;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.metadata.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mule.module.apikit.helpers.AttributesHelper.getMediaType;

public class BodyValidator {

  protected final static Logger logger = LoggerFactory.getLogger(BodyValidator.class);

  public static ValidBody validate(IAction action, HttpRequestAttributes attributes, Object payload,
                                   ValidationConfig config, String charset)
      throws BadRequestException, UnsupportedMediaTypeException {
    return validate(action, attributes, payload, config, charset, null);
  }

  public static ValidBody validate(IAction action, HttpRequestAttributes attributes, Object payload,
                                   ValidationConfig config, String charset, ErrorTypeRepository errorTypeRepository)
      throws BadRequestException, UnsupportedMediaTypeException {

    ValidBody validBody = new ValidBody(payload);

    if (action == null || !action.hasBody()) {
      logger.debug("=== no body types defined: accepting any request content-type");
      return validBody;
    }

    final String requestMimeTypeName = getMediaType(attributes);

    final Entry<String, IMimeType> foundMimeType = action.getBody().entrySet().stream()
        .peek(entry -> {
          if (logger.isDebugEnabled())
            logger.debug(format("comparing request media type %s with expected %s\n", requestMimeTypeName, entry.getKey()));
        })
        .filter(entry -> getMediaType(entry.getKey()).equals(requestMimeTypeName))
        .findFirst()
        .orElseThrow(UnsupportedMediaTypeException::new);


    final IMimeType mimeType = foundMimeType.getValue();

    if (requestMimeTypeName.contains("json") || requestMimeTypeName.contains("xml")) {

      validBody = validateAsString(config, mimeType, action, requestMimeTypeName, payload, charset, errorTypeRepository);

    } else if ((requestMimeTypeName.contains("multipart/form-data")
        || requestMimeTypeName.contains("application/x-www-form-urlencoded"))) {

      validBody = validateAsMultiPart(config, mimeType, requestMimeTypeName, payload);

    }

    return validBody;
  }

  private static ValidBody validateAsString(ValidationConfig config, IMimeType mimeType, IAction action,
                                            String requestMimeTypeName,
                                            Object payload, String charset)
      throws BadRequestException {
    return validateAsString(config, mimeType, action, requestMimeTypeName, payload, charset, null);
  }

  private static ValidBody validateAsString(ValidationConfig config, IMimeType mimeType, IAction action,
                                            String requestMimeTypeName,
                                            Object payload, String charset, ErrorTypeRepository errorTypeRepository)
      throws BadRequestException {

    IRestSchemaValidatorStrategy validator = null;
    if (config.isParserV2()) {
      validator = new RestSchemaV2Validator(mimeType);
    } else {
      String schemaPath = SchemaCacheUtils.getSchemaCacheKey(action, requestMimeTypeName);

      try {
        if (requestMimeTypeName.contains("json")) {
          ApiKitJsonSchema schema = config.getJsonSchema(schemaPath);
          validator = new RestJsonSchemaValidator(schema != null ? schema.getSchema() : null);
        } else if (requestMimeTypeName.contains("xml")) {
          validator = new RestXmlSchemaValidator(config.getXmlSchema(schemaPath), errorTypeRepository);
        }
      } catch (ExecutionException e) {
        throw new BadRequestException(e);
      }
    }

    if (validator == null) {
      throw new BadRequestException(format("Unexpected Mime Type %s", requestMimeTypeName));
    }

    final ValidBody validBody = new ValidBody(payload);

    validator.validate(getPayloadAsString(validBody.getPayload(), charset));

    return validBody;
  }

  private static ValidBody validateAsMultiPart(ValidationConfig config, IMimeType mimeType, String requestMimeTypeName,
                                               Object payload)
      throws BadRequestException {

    final ValidBody validBody = new ValidBody(payload);

    final TypedValue payloadAsTypedValue = validBody.getPayloadAsTypedValue();

    final FormParametersValidator formParametersValidator;
    if (mimeType.getFormParameters() != null) {

      if (requestMimeTypeName.contains("multipart/form-data")) {

        formParametersValidator =
            new FormParametersValidator(new MultipartFormValidator(mimeType.getFormParameters(), config.getExpressionManager()));
        validBody.setFormParameters(formParametersValidator.validate(payloadAsTypedValue));

      } else if (requestMimeTypeName.contains("application/x-www-form-urlencoded")) {
        if (config.isParserV2()) {
          formParametersValidator =
              new FormParametersValidator(new UrlencodedFormV2Validator(mimeType, config.getExpressionManager()));

        } else {
          formParametersValidator = new FormParametersValidator(new UrlencodedFormV1Validator(mimeType.getFormParameters(),
                                                                                              config.getExpressionManager()));
        }

        validBody.setFormParameters(formParametersValidator.validate(payloadAsTypedValue));
      }
    }

    return validBody;
  }

}
