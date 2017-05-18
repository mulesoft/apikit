/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.exception.InvalidFormParameterException;
import org.mule.module.apikit.exception.InvalidHeaderException;
import org.mule.module.apikit.exception.InvalidQueryParameterException;
import org.mule.module.apikit.exception.InvalidUriParameterException;
import org.mule.module.apikit.exception.MethodNotAllowedException;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.exception.ErrorTypeRepositoryFactory;
import org.mule.runtime.core.exception.TypedException;

public enum ApikitErrorTypes
{
    NOT_FOUND("APIKIT:NOT_FOUND"),
    METHOD_NOT_ALLOWED("APIKIT:METHOD_NOT_ALLOWED"),
    UNSUPPORTED_MEDIA_TYPE("APIKIT:UNSUPPORTED_MEDIA_TYPE"),
    NOT_ACCEPTABLE("APIKIT:NOT_ACCEPTABLE"),
    INVALID_URI_PARAMETER("APIKIT:BAD_REQUEST"),
    INVALID_HEADER("APIKIT:BAD_REQUEST"),
    INVALID_QUERY_PARAMETER("APIKIT:BAD_REQUEST"),
    INVALID_FORM_PARAMETER("APIKIT:BAD_REQUEST"),
    BAD_REQUEST("APIKIT:BAD_REQUEST");

    private String internalName;

    ApikitErrorTypes(String internalName) {
        this.internalName = internalName;
    }

    public String getInternalName() {
        return internalName;
    }

    private static MuleContext muleContext;

    public static void initialise(MuleContext muleContext)
    {
        ApikitErrorTypes.muleContext = muleContext;
    }

    public TypedException throwErrorType(String value)
    {
        ComponentIdentifier componentIdentifier = ComponentIdentifier.buildFromStringRepresentation(internalName);
        Throwable exception;
        switch (this){
            case NOT_FOUND:
                exception = new NotFoundException(value);
                break;
            case METHOD_NOT_ALLOWED:
                exception = new MethodNotAllowedException(value);
                break;
            case INVALID_URI_PARAMETER:
                exception = new InvalidUriParameterException(value);
                break;
            case INVALID_HEADER:
                exception = new InvalidHeaderException(value);
                break;
            case INVALID_QUERY_PARAMETER:
                exception = new InvalidQueryParameterException(value);
                break;
            case BAD_REQUEST:
                exception = new BadRequestException(value);
                break;
            case INVALID_FORM_PARAMETER:
                exception = new InvalidFormParameterException(value);
                break;
            case UNSUPPORTED_MEDIA_TYPE:
                exception = new UnsupportedMediaTypeException(value);
                break;
            case NOT_ACCEPTABLE:
                exception = new NotAcceptableException(value);
                break;
            default:
                exception = new DefaultMuleException(value);
        }
        if (muleContext != null)
        {
            ErrorType errorType = muleContext.getErrorTypeRepository().getErrorType(componentIdentifier).get();
            if (errorType != null)
            {
                return new TypedException(exception, errorType);
            }
        }
        return new TypedException(exception, ErrorTypeRepositoryFactory.createDefaultErrorTypeRepository().getAnyErrorType());
    }

    public TypedException throwErrorType(Throwable value)
    {
        ComponentIdentifier componentIdentifier = ComponentIdentifier.buildFromStringRepresentation(internalName);
        Throwable exception;
        switch (this){
            case NOT_FOUND:
                exception = new NotFoundException(value);
                break;
            case METHOD_NOT_ALLOWED:
                exception = new MethodNotAllowedException(value);
                break;
            case INVALID_URI_PARAMETER:
                exception = new InvalidUriParameterException(value);
                break;
            case INVALID_HEADER:
                exception = new InvalidHeaderException(value);
                break;
            case INVALID_QUERY_PARAMETER:
                exception = new InvalidQueryParameterException(value);
                break;
            case BAD_REQUEST:
                exception = new BadRequestException(value);
                break;
            case INVALID_FORM_PARAMETER:
                exception = new InvalidFormParameterException(value);
                break;
            case UNSUPPORTED_MEDIA_TYPE:
                exception = new UnsupportedMediaTypeException(value);
                break;
            case NOT_ACCEPTABLE:
                exception = new NotAcceptableException(value);
                break;
            default:
                exception = new DefaultMuleException(value);
                break;
        }
        if (muleContext != null)
        {
            ErrorType errorType = muleContext.getErrorTypeRepository().getErrorType(componentIdentifier).get();
            if (errorType != null)
            {
                return new TypedException(exception, errorType);
            }
        }
        return new TypedException(exception, ErrorTypeRepositoryFactory.createDefaultErrorTypeRepository().getAnyErrorType());

    }

    public TypedException throwErrorType()
    {
        ComponentIdentifier componentIdentifier = ComponentIdentifier.buildFromStringRepresentation(internalName);
        Throwable exception;
        switch (this){
            case NOT_FOUND:
                exception = new NotFoundException();
                break;
            case METHOD_NOT_ALLOWED:
                exception = new MethodNotAllowedException();
                break;
            case INVALID_URI_PARAMETER:
                exception = new InvalidUriParameterException();
                break;
            case INVALID_HEADER:
                exception = new InvalidHeaderException();
                break;
            case INVALID_QUERY_PARAMETER:
                exception = new InvalidQueryParameterException();
                break;
            case BAD_REQUEST:
                exception = new BadRequestException();
                break;
            case INVALID_FORM_PARAMETER:
                exception = new InvalidFormParameterException();
                break;
            case UNSUPPORTED_MEDIA_TYPE:
                exception = new UnsupportedMediaTypeException();
                break;
            case NOT_ACCEPTABLE:
                exception = new NotAcceptableException();
                break;
            default:
                exception = new DefaultMuleException("");
        }
        if (muleContext != null)
        {
            ErrorType errorType = muleContext.getErrorTypeRepository().getErrorType(componentIdentifier).get();
            if (errorType != null)
            {
                return new TypedException(exception, errorType);
            }
        }
        return new TypedException(exception, ErrorTypeRepositoryFactory.createDefaultErrorTypeRepository().getAnyErrorType());
    }
}
