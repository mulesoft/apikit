/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import static org.mule.module.apikit.api.UrlUtils.getRedirectLocation;

import java.util.HashMap;
import java.util.Map;

import org.mule.extension.http.api.HttpHeaders;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.http.api.HttpConstants;

public class EventWrapper {

  private CoreEvent inputEvent;
  private CoreEvent.Builder outputBuilder;
  private HashMap<String, String> outboundHeaders = new HashMap<>();
  private String httpStatus;
  private String outboundHeadersMapName;
  private String httpStatusVarName;


  public EventWrapper(CoreEvent input, String outboundHeadersMapName, String httpStatusVarName) {
    inputEvent = input;
    outputBuilder = CoreEvent.builder(input);
    this.outboundHeadersMapName = outboundHeadersMapName;
    this.httpStatusVarName = httpStatusVarName;
    httpStatus = String.valueOf(HttpConstants.HttpStatus.OK.getStatusCode());
  }

  public void addOutboundProperties(Map<String, String> headers) {
    outboundHeaders.putAll(headers);
  }

  public CoreEvent build() {
    outputBuilder.addVariable(httpStatusVarName, httpStatus);
    outputBuilder.addVariable(outboundHeadersMapName, outboundHeaders);
    return outputBuilder.build();
  }


  public EventWrapper doClientRedirect() {
    httpStatus = String.valueOf(HttpConstants.HttpStatus.MOVED_PERMANENTLY.getStatusCode());

    HttpRequestAttributes attributes = EventHelper.getHttpRequestAttributes(inputEvent);
    String scheme = attributes.getScheme();
    String remoteAddress = attributes.getHeaders().get("host");
    String queryString = attributes.getQueryString();
    String requestPath = attributes.getRequestPath();

    String redirectLocation = getRedirectLocation(scheme, remoteAddress, requestPath, queryString);
    outboundHeaders.put(HttpHeaders.Names.LOCATION, redirectLocation);
    return this;
  }



  public EventWrapper setPayload(Object payload, MediaType mediaType) {
    outputBuilder.message(MessageHelper.setPayload(inputEvent.getMessage(), payload, mediaType));
    return this;
  }
}
