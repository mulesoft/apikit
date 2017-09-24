/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;


import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.validation.ValidRequest;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;

import java.nio.charset.Charset;
import java.util.Optional;

public class EventHelper {

  private EventHelper() {

  }

  public static Charset getEncoding(CoreEvent event) {
    return getEncoding(event.getMessage());
  }

  public static Charset getEncoding(Message message) {
    Optional<Charset> payloadEncoding = message.getPayload().getDataType().getMediaType().getCharset();
    return payloadEncoding.orElse(Charset.defaultCharset());// TODO Should we get default charset from mule?
  }

  public static CoreEvent.Builder regenerateEvent(Message message, CoreEvent.Builder builder, ValidRequest validRequest) {
    Message.Builder messageBuilder = Message.builder(message);
    messageBuilder.value(validRequest.getBody().getPayload());
    messageBuilder.attributesValue(validRequest.getAttributes());

    return builder.message(messageBuilder.build());
  }

  public static HttpRequestAttributes getHttpRequestAttributes(CoreEvent event) {
    return ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());
  }

}
