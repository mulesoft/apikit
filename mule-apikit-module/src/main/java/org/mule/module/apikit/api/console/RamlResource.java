/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.console;

import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.util.MultiMap;

public class RamlResource implements Resource {

  String content;

  public RamlResource(String content) {
    this.content = content;
  }

  public MediaType getMediaType() {
    return MediaType.create("application", "raml+yaml");
  }

  public String getContent() {
    return content;
  }

  @Override
  public MultiMap<String, String> getHeaders() {
    return new MultiMap<>();
  }
}
