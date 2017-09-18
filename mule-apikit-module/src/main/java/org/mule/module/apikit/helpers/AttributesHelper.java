/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import com.google.common.base.Strings;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.HeaderNames;
import org.mule.runtime.api.util.MultiMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Map;

public class AttributesHelper {

  private AttributesHelper() {
    // Prevents instantiation :)
  }

  public static MultiMap addParam(MultiMap<String, String> oldParams, String key, String value) {
    MultiMap<String, String> mapParam = new MultiMap<>();
    LinkedList<String> valueList = new LinkedList<>();
    valueList.add(value);
    mapParam.put(key, valueList);
    for (Map.Entry<String, String> entry : oldParams.entrySet()) {
      LinkedList<String> list = new LinkedList<>();
      list.add(entry.getValue());
      mapParam.put(entry.getKey(), list);
    }
    return mapParam;
  }

  public static String addQueryString(String oldQueryString, int queryStringSize, String key, String value) {
    String newParam = queryStringSize != 0 ? "&" : "";
    try {
      newParam += URLEncoder.encode(key, "UTF-8");
      if (value != null) {

        newParam += "=" + URLEncoder.encode(value, "UTF-8");
      }
    } catch (UnsupportedEncodingException e) {
      //UTF-8 will never be unsupported
    }
    return oldQueryString + newParam;
  }

  public static HttpRequestAttributes replaceParams(HttpRequestAttributes attributes, MultiMap<String, String> headers,
                                                    MultiMap<String, String> queryParams, String queryString,
                                                    MultiMap<String, String> uriParams) {
    return new HttpRequestAttributes(headers, attributes.getListenerPath(), attributes.getRelativePath(),
                                     attributes.getVersion(), attributes.getScheme(),
                                     attributes.getMethod(), attributes.getRequestPath(),
                                     attributes.getRequestUri(), queryString,
                                     queryParams, uriParams,
                                     attributes.getRemoteAddress(), attributes.getClientCertificate());
  }

  private static String ANY_RESPONSE_MEDIA_TYPE = "*/*";

  public static String getHeaderIgnoreCase(HttpRequestAttributes attributes, String name) {
    MultiMap<String, String> headers = attributes.getHeaders();
    return getParamIgnoreCase(headers, name);
  }

  public static String getParamIgnoreCase(MultiMap<String, String> parameters, String name) {
    for (String header : parameters.keySet()) {
      if (header.equalsIgnoreCase(name.toLowerCase())) {
        return parameters.get(header);
      }
    }
    return null;
  }

  public static String getMediaType(HttpRequestAttributes attributes) {
    String contentType = getHeaderIgnoreCase(attributes, HeaderNames.CONTENT_TYPE.getName());
    return contentType != null ? contentType.split(";")[0] : null;
  }

  public static String getAcceptedResponseMediaTypes(MultiMap<String, String> headers) {
    String acceptableResponseMediaTypes = getParamIgnoreCase(headers, "accept");
    if (Strings.isNullOrEmpty(acceptableResponseMediaTypes)) {
      return ANY_RESPONSE_MEDIA_TYPE;
    }
    return acceptableResponseMediaTypes;
  }

}
