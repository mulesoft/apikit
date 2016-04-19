/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model;

import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.Map;

public interface IResponse
{
    Map<String, IMimeType> getBody();
    boolean hasBody();
    Map<String, IParameter> getHeaders();
    void setBody(Map<String, IMimeType> body);
    void setHeaders(Map<String, IParameter> headers);
    Object getInstance();
}
