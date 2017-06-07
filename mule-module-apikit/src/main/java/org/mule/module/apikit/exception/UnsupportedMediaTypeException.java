/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.exception;

public class UnsupportedMediaTypeException extends MuleRestException {

    private static String STRING_REPRESENTATION = "APIKIT:UNSUPPORTED_MEDIA_TYPE";

    public UnsupportedMediaTypeException(String path) {
        super(path);
    }

    public UnsupportedMediaTypeException(Throwable t) {
        super(t);
    }

    public UnsupportedMediaTypeException(){
        super();
    }

    @Override
    public String getStringRepresentation()
    {
        return STRING_REPRESENTATION;
    }
}
