/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.runtime.core.api.exception.Errors.CORE_NAMESPACE_NAME;
import static org.mule.runtime.core.api.exception.Errors.Identifiers.ANY_IDENTIFIER;

import java.util.Optional;

import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.exception.TypedException;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.internal.message.ErrorTypeBuilder;


public class ApikitErrorTypes {

  public static TypedException throwErrorType(MuleRestException exception, ErrorTypeRepository errorTypeRepository) {
    ComponentIdentifier componentIdentifier =
        ComponentIdentifier.buildFromStringRepresentation(exception.getStringRepresentation());
    if (errorTypeRepository != null) {
      Optional<ErrorType> errorType = errorTypeRepository.getErrorType(componentIdentifier);
      if (errorType.isPresent()) {
        return new TypedException(exception, errorType.get());
      }
    }
    return new TypedException(exception,
                              ErrorTypeBuilder.builder().namespace(CORE_NAMESPACE_NAME).identifier(ANY_IDENTIFIER).build());
  }

  public static ErrorTypeRepository errorRepositoryFrom(MuleContext muleContext) {
    return muleContext != null ? muleContext.getErrorTypeRepository() : null;
  }
}
