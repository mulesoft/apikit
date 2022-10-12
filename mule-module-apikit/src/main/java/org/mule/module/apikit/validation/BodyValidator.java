/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.mule.module.apikit.exception.BadRequestException;
import org.mule.raml.implv2.v10.model.MimeTypeImpl;
import org.mule.raml.interfaces.model.IMimeType;
import org.raml.v2.api.model.common.ValidationResult;

import java.util.List;

public class BodyValidator {

  private final IMimeType mimeType;

  public BodyValidator(IMimeType mimeType) {
    this.mimeType = mimeType;
  }

  public void validateSchemaV2(boolean isJsonMimeType, String payload)
      throws BadRequestException {
    List<ValidationResult> validationResults;
    if (mimeType instanceof MimeTypeImpl) {
      // TODO : remove this, payload must be validated in raml-parser
      if (isJsonMimeType && !payloadIsJson(payload)) {
        throw new BadRequestException("Expected JSON body");
      }
      if (!isJsonMimeType && !payloadIsXML(payload)) {
        throw new BadRequestException("Expected XML body");
      }
      validationResults = ((MimeTypeImpl) mimeType)
          .validate(payload);
    } else {
      throw new RuntimeException("not supported");
    }
    if (!validationResults.isEmpty()) {
      String message = validationResults.get(0).getMessage();
      throw new BadRequestException(message);
    }
  }

  private boolean payloadIsXML(String value) {
    if (value == null) {
      return true;
    }
    return value.trim().startsWith("<");
  }

  private boolean payloadIsJson(String value) {
    if (value == null) {
      return true;
    }
    return value.trim().startsWith("{") || value.trim().startsWith("[");
  }

}
