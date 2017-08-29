/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;

public class MessageHelper {

  public static Message setPayload(Message message, Object payload, String mimetype) {
    return setPayload(message, payload, MediaType.parse(mimetype));
  }

  public static Message setPayload(Message message, Object payload, MediaType mediatype) {
    Message.Builder messageBuilder = Message.builder(message);
    messageBuilder.value(payload);
    if (mediatype != null) {
      messageBuilder.mediaType(mediatype);
    }
    return messageBuilder.build();
  }
}
