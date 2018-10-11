/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model;

import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.Map;

public interface IQueryString {

  String getDefaultValue();

  boolean isArray();

  boolean validate(String value);

  boolean isScalar();

  boolean isFacetArray(String facet);

  Map<String, IParameter> facets();

  String surroundWithQuotesIfNeeded(String facet, String value);
}
