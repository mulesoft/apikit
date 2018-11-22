/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model.parameter;

import java.util.Map;

public interface IParameter
{
    boolean isRequired();
    String getDefaultValue();
    @Deprecated
    boolean isRepeat();
    @Deprecated
    boolean isArray();
    boolean validate(String value);
    void validate(String expectedKey, Object values, String parameterType) throws Exception;
    String message(String value);
    String getDisplayName();
    String getDescription();
    String getExample();
    Map<String, String> getExamples();
    Object getInstance();
    @Deprecated
    boolean isStringArray();
    @Deprecated
    boolean isScalar();
    @Deprecated
    boolean isFacetArray(String facet);
}
