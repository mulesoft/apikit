/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model;

import java.util.List;
import java.util.Map;

import org.mule.raml.interfaces.model.parameter.IParameter;

public interface IResource
{
    IAction getAction(String name);
    String getUri();
    String getResolvedUri(String version);
    void setParentUri(String parentUri);
    Map<String, IResource> getResources();
    String getParentUri();
    Map<IActionType, IAction> getActions();
    Map<String, List<IParameter>> getBaseUriParameters();
    Map<String, IParameter> getResolvedUriParameters();
    String getDisplayName();
    String getRelativeUri();
    void cleanBaseUriParameters();
}
