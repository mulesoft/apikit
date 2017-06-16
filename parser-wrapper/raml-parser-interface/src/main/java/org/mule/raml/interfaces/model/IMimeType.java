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

public interface IMimeType
{
    Object getCompiledSchema();
    String getSchema();
    Map<String, List<IParameter>> getFormParameters();

    /**
     * @return the mime type name (e.g: application/json)
     */
    String getType();

    String getExample();
    Object getInstance();
}
