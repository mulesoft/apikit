/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.helpers.PayloadHelper;
import org.mule.module.apikit.validation.body.form.FormParametersValidator;
import org.mule.module.apikit.validation.body.form.MultipartFormValidator;
import org.mule.module.apikit.validation.body.form.UrlencodedFormV2Validator;
import org.mule.module.apikit.validation.body.form.UrlencodedFormValidator;
import org.mule.module.apikit.validation.body.schema.RestSchemaValidator;
import org.mule.module.apikit.validation.body.schema.v1.RestJsonSchemaValidator;
import org.mule.module.apikit.validation.body.schema.v1.RestXmlSchemaValidator;
import org.mule.module.apikit.validation.body.schema.v1.cache.SchemaCacheUtils;
import org.mule.module.apikit.validation.body.schema.v2.RestSchemaV2Validator;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BodyValidator {

  protected final static Logger logger = LoggerFactory.getLogger(BodyValidator.class);


  public static ValidBody validate(IAction action, HttpRequestAttributes attributes, Object payload,
                                ValidationConfig config, String charset)
      throws BadRequestException {

    ValidBody validBody = new ValidBody(payload);

    if (action == null || !action.hasBody()) {
      logger.debug("=== no body types defined: accepting any request content-type");
      return validBody;
    }

    String requestMimeTypeName = AttributesHelper.getMediaType(attributes);
    Map.Entry<String, IMimeType> foundMimeType;

    try {

      foundMimeType = action.getBody()
          .entrySet()
          .stream()
          .filter(entry -> {
            if (logger.isDebugEnabled()) {
              logger.debug(String.format("comparing request media type %s with expected %s\n", requestMimeTypeName,
                                         entry.getKey()));
            }

            return entry.getKey().equals(requestMimeTypeName);
          })
          .findFirst()
          .get();

    } catch (NoSuchElementException e) {
      throw ApikitErrorTypes.throwErrorTypeNew(new UnsupportedMediaTypeException());
    }


    IMimeType mimeType = foundMimeType.getValue();


    if(requestMimeTypeName.contains("json") || requestMimeTypeName.contains("xml")) {

      validateAsString(config, mimeType, action, requestMimeTypeName, payload, charset);

    } else if(requestMimeTypeName.contains("multipart/form-data") || requestMimeTypeName.contains("application/x-www-form-urlencoded")) {

      validBody = validateAsMultiPart(config, mimeType, requestMimeTypeName, payload);

    }

    return validBody;
  }

  private static void validateAsString(ValidationConfig config, IMimeType mimeType, IAction action, String requestMimeTypeName, Object payload, String charset) throws BadRequestException {
    RestSchemaValidator schemaValidator = null;

    if (config.isParserV2()) {
      schemaValidator = new RestSchemaValidator(new RestSchemaV2Validator(mimeType));
    } else {
      String schemaPath = SchemaCacheUtils.getSchemaCacheKey(action, requestMimeTypeName);

      try {
        if (requestMimeTypeName.contains("json")) {

          schemaValidator = new RestSchemaValidator(new RestJsonSchemaValidator(config.getJsonSchema(schemaPath).getSchema()));

        } else if(requestMimeTypeName.contains("xml")) {
          schemaValidator = new RestSchemaValidator(new RestXmlSchemaValidator(config.getXmlSchema(schemaPath)));
        }
      } catch (ExecutionException e) {
        throw ApikitErrorTypes.throwErrorTypeNew(new BadRequestException(e));
      }
    }


    String strPayload = PayloadHelper.getPayloadAsString(payload, charset, requestMimeTypeName.contains("json"));

    schemaValidator.validate(strPayload);

  }

  private static ValidBody validateAsMultiPart(ValidationConfig config, IMimeType mimeType, String requestMimeTypeName, Object payload) throws BadRequestException {
    ValidBody validBody = new ValidBody(payload);
    FormParametersValidator formParametersValidator = null;

    if (mimeType.getFormParameters() != null) {

      if (requestMimeTypeName.contains("multipart/form-data")) {

        formParametersValidator = new FormParametersValidator(new MultipartFormValidator(mimeType.getFormParameters()));
        validBody.setFormParameters(formParametersValidator.validate(payload));

      } else if (requestMimeTypeName.contains("application/x-www-form-urlencoded")) {
        if (config.isParserV2()) {
          formParametersValidator = new FormParametersValidator(new UrlencodedFormV2Validator(mimeType));

        } else {
          formParametersValidator = new FormParametersValidator(new UrlencodedFormValidator(mimeType.getFormParameters()));
        }

        validBody.setFormParameters(formParametersValidator.validate(payload));
      }
    }

    return validBody;
  }

}
