/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.HeaderNames;
import org.mule.runtime.http.api.domain.ParameterMap;

public class AttributesHelper
{
  private AttributesHelper()
  {
      // Prevents instantiation :)
  }

  //public static HttpRequestAttributes addHeader(HttpRequestAttributes oldHttpRequestAttributes, String key, String value) {
  //  Map<String, LinkedList<String>> mapHeaders = new HashMap<>();
  //  LinkedList<String> valueList = new LinkedList<>();
  //  valueList.add(value);
  //  mapHeaders.put(key, valueList);
  //  for (Map.Entry<String, String> entry : oldHttpRequestAttributes.getHeaders().entrySet()) {
  //    LinkedList<String> list = new LinkedList<>();
  //    list.add(entry.getValue());
  //    mapHeaders.put(entry.getKey(), list);
  //  }
  //  ParameterMap headers = new ParameterMap(mapHeaders);
  //  return new HttpRequestAttributes(headers, oldHttpRequestAttributes.getListenerPath(),
  //                                   oldHttpRequestAttributes.getRelativePath(), oldHttpRequestAttributes.getVersion(),
  //                                   oldHttpRequestAttributes.getScheme(), oldHttpRequestAttributes.getMethod(),
  //                                   oldHttpRequestAttributes.getRequestPath(), oldHttpRequestAttributes.getRequestUri(),
  //                                   oldHttpRequestAttributes.getQueryString(), oldHttpRequestAttributes.getQueryParams(),
  //                                   oldHttpRequestAttributes.getUriParams(), oldHttpRequestAttributes.getRemoteAddress(),
  //                                   oldHttpRequestAttributes.getClientCertificate());
  //}
  //
  //public static ParameterMap addHeader(ParameterMap oldHeaders, String key, String value) {
  //  Map<String, LinkedList<String>> mapHeaders = new HashMap<>();
  //  LinkedList<String> valueList = new LinkedList<>();
  //  valueList.add(value);
  //  mapHeaders.put(key, valueList);
  //  for (Map.Entry<String, String> entry : oldHeaders.entrySet()) {
  //    LinkedList<String> list = new LinkedList<>();
  //    list.add(entry.getValue());
  //    mapHeaders.put(entry.getKey(), list);
  //  }
  //  return new ParameterMap(mapHeaders);
  //  //return new HttpRequestAttributes(headers, oldHttpRequestAttributes.getListenerPath(), oldHttpRequestAttributes.getRelativePath(), oldHttpRequestAttributes.getVersion(), oldHttpRequestAttributes.getScheme(), oldHttpRequestAttributes.getMethod(), oldHttpRequestAttributes.getRequestPath(), oldHttpRequestAttributes.getRequestUri(), oldHttpRequestAttributes.getQueryString(), oldHttpRequestAttributes.getQueryParams(), oldHttpRequestAttributes.getUriParams(), oldHttpRequestAttributes.getRemoteAddress(), oldHttpRequestAttributes.getClientCertificate());
  //}
  //
  //public static HttpRequestAttributes addQueryParam(HttpRequestAttributes oldHttpRequestAttributes, String key, String value) {
  //  Map<String, LinkedList<String>> mapQueryParam = new HashMap<>();
  //  LinkedList<String> valueList = new LinkedList<>();
  //  valueList.add(value);
  //  mapQueryParam.put(key, valueList);
  //  for (Map.Entry<String, String> entry : oldHttpRequestAttributes.getQueryParams().entrySet()) {
  //    LinkedList<String> list = new LinkedList<>();
  //    list.add(entry.getValue());
  //    mapQueryParam.put(entry.getKey(), list);
  //  }
  //  String newParam = oldHttpRequestAttributes.getQueryParams().size() != 0 ? "&" : "";
  //  newParam += key;
  //  if (value != null) {
  //    newParam += "=" + value;
  //  }
  //  ParameterMap queryParam = new ParameterMap(mapQueryParam);
  //
  //  String newQueryString = oldHttpRequestAttributes.getQueryString() + newParam;
  //  return new HttpRequestAttributes(oldHttpRequestAttributes.getHeaders(), oldHttpRequestAttributes.getListenerPath(),
  //                                   oldHttpRequestAttributes.getRelativePath(), oldHttpRequestAttributes.getVersion(),
  //                                   oldHttpRequestAttributes.getScheme(), oldHttpRequestAttributes.getMethod(),
  //                                   oldHttpRequestAttributes.getRequestPath(), oldHttpRequestAttributes.getRequestUri(),
  //                                   newQueryString, queryParam, oldHttpRequestAttributes.getUriParams(),
  //                                   oldHttpRequestAttributes.getRemoteAddress(),
  //                                   oldHttpRequestAttributes.getClientCertificate());
  //}

  public static ParameterMap addParam(ParameterMap oldParams, String key, String value) {
    Map<String, LinkedList<String>> mapParam = new HashMap<>();
    LinkedList<String> valueList = new LinkedList<>();
    valueList.add(value);
    mapParam.put(key, valueList);
    for (Map.Entry<String, String> entry : oldParams.entrySet()) {
      LinkedList<String> list = new LinkedList<>();
      list.add(entry.getValue());
      mapParam.put(entry.getKey(), list);
    }
    return new ParameterMap(mapParam);
  }

  public static String addQueryString(String oldQueryString, int queryStringSize, String key, String value) {
    String newParam = queryStringSize != 0 ? "&" : "";
    newParam += key;
    if (value != null) {
      newParam += "=" + value;
    }
    return oldQueryString + newParam;
  }

  public static HttpRequestAttributes replaceParams(HttpRequestAttributes attributes, ParameterMap headers, ParameterMap queryParams, String queryString, ParameterMap uriParams)
  {
    return new HttpRequestAttributes(headers, attributes.getListenerPath(), attributes.getRelativePath(),
                                     attributes.getVersion(), attributes.getScheme(),
                                     attributes.getMethod(), attributes.getRequestPath(),
                                     attributes.getRequestUri(), queryString,
                                     queryParams, uriParams,
                                     attributes.getRemoteAddress(), attributes.getClientCertificate());
  }

  private static String ANY_RESPONSE_MEDIA_TYPE = "*/*";

  public static String getHeaderIgnoreCase(HttpRequestAttributes attributes, String name)
  {
    ParameterMap headers = attributes.getHeaders();
    return getParamIgnoreCase(headers, name);
  }

  public static String getParamIgnoreCase(ParameterMap parameters, String name)
  {
    for (String header : parameters.keySet())
    {
      if (header.equalsIgnoreCase(name.toLowerCase()))
      {
        return parameters.get(header);
      }
    }
    return null;
  }

  public static String getMediaType(HttpRequestAttributes attributes)
  {
    String contentType = getHeaderIgnoreCase(attributes, HeaderNames.CONTENT_TYPE);
    return contentType != null ? contentType.split(";")[0] : null;
  }

  public static String getAcceptedResponseMediaTypes(ParameterMap headers)
  {
    String acceptableResponseMediaTypes = getParamIgnoreCase(headers, "accept");
    if (acceptableResponseMediaTypes == null || acceptableResponseMediaTypes == "")
    {
      return ANY_RESPONSE_MEDIA_TYPE;
    }
    return acceptableResponseMediaTypes;
  }

  public static boolean isAnAcceptedResponseMediaType(HttpRequestAttributes attributes, String candidateMediaType)
  {
    String acceptedResponseMediaTypes = getAcceptedResponseMediaTypes(attributes.getHeaders());
    if (acceptedResponseMediaTypes.equals(ANY_RESPONSE_MEDIA_TYPE))
    {
      return true;
    }
    return acceptedResponseMediaTypes.contains(candidateMediaType);
  }

}
