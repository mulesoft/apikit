/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model;

import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.List;
import java.util.Map;

public interface IAction
{
    IActionType getType();
    IResource getResource();
    Map<String, IMimeType> getBody();
    Map<String, List<IParameter>> getBaseUriParameters();
    Map<String, IParameter> getQueryParameters();
    @Deprecated
    IParameter getQueryString();
    IQueryString queryString();
    boolean hasBody();
    Map<String, IResponse> getResponses();
    Map<String, IParameter> getHeaders();
    List<ISecurityReference> getSecuredBy();
    List<String> getIs();
    void cleanBaseUriParameters();
    void setHeaders(Map<String, IParameter> headers);
    void setQueryParameters(Map<String, IParameter> queryParameters);
    void setBody(Map<String, IMimeType> body);
    void addResponse(String key, IResponse response);
    void addSecurityReference(String securityReferenceName);
    void addIs(String is);
}
