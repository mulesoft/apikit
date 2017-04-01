/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.AttributesRegenerator;
import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.UrlUtils;
import org.mule.module.apikit.exception.InvalidHeaderException;
import org.mule.module.apikit.exception.InvalidQueryParameterException;
import org.mule.module.apikit.exception.MethodNotAllowedException;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.uri.ResolvedVariables;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolver;
import org.mule.module.apikit.validation.attributes.HeadersValidator;
import org.mule.module.apikit.validation.attributes.QueryParameterValidator;
import org.mule.module.apikit.validation.attributes.UriParametersValidator;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.service.http.api.domain.ParameterMap;

import com.sun.tools.javac.comp.Resolve;

import java.util.concurrent.ExecutionException;

//import org.mule.runtime.core.api.DefaultMuleException;

public class AttributesValidatior {

  Configuration configuration;
  ParameterMap headers;
  ParameterMap queryParams;
  String queryString;
  ParameterMap uriParams;

  public AttributesValidatior(Configuration configuration) {
    this.configuration = configuration;
  }


  public HttpRequestAttributes validateAndAddDefaults(HttpRequestAttributes attributes, IResource resource, ResolvedVariables resolvedVariables)
          throws MuleRestException, DefaultMuleException
  {
    //uriparams
    UriParametersValidator uriParametersValidator = new UriParametersValidator(resource, resolvedVariables);
    uriParams = uriParametersValidator.validateAndAddDefaults(attributes.getUriParams());

    //queryparams
    QueryParameterValidator queryParamValidator =
        new QueryParameterValidator(resource.getAction(attributes.getMethod().toLowerCase()));
    queryParamValidator.validateAndAddDefaults(attributes.getQueryParams(), attributes.getQueryString());
    queryParams = queryParamValidator.getQueryParams();
    queryString = queryParamValidator.getQueryString();

    //headers
    HeadersValidator headersValidator = new HeadersValidator();
    headersValidator.validateAndAddDefaults(attributes.getHeaders(), resource.getAction(attributes.getMethod().toLowerCase()));
    headers = headersValidator.getNewHeaders();

    //regenerate attributes
    return AttributesRegenerator.replaceParams(attributes, headers, queryParams, queryString, uriParams);
  }

}
