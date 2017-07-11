/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.validation.attributes.HeadersValidator;
import org.mule.module.apikit.validation.attributes.QueryParameterValidator;
import org.mule.module.apikit.validation.attributes.UriParametersValidator;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.DefaultMuleException;

public class AttributesValidator {

  public static HttpRequestAttributes validateAndAddDefaults(HttpRequestAttributes attributes, IResource resource,
                                                             ResolvedVariables resolvedVariables)
      throws MuleRestException, DefaultMuleException {

    MultiMap<String, String> headers;
    MultiMap<String, String> queryParams;
    String queryString;
    MultiMap<String, String> uriParams;

    // uriparams
    UriParametersValidator uriParametersValidator = new UriParametersValidator(resource, resolvedVariables);
    uriParams = uriParametersValidator.validateAndAddDefaults(attributes.getUriParams());

    // queryparams
    QueryParameterValidator queryParamValidator =
        new QueryParameterValidator(resource.getAction(attributes.getMethod().toLowerCase()));
    queryParamValidator.validateAndAddDefaults(attributes.getQueryParams(), attributes.getQueryString());
    queryParams = queryParamValidator.getQueryParams();
    queryString = queryParamValidator.getQueryString();

    // headers
    HeadersValidator headersValidator = new HeadersValidator();
    headersValidator.validateAndAddDefaults(attributes.getHeaders(), resource.getAction(attributes.getMethod().toLowerCase()));
    headers = headersValidator.getNewHeaders();

    // regenerate attributes
    return AttributesHelper.replaceParams(attributes, headers, queryParams, queryString, uriParams);
  }

}
