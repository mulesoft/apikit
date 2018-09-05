/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.loader;

import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

import javax.annotation.Nullable;
import java.io.InputStream;

import static java.lang.String.format;

public class ApiSyncResourceLoader implements ResourceLoader {

  private ResourceLoader resourceLoader = new DefaultResourceLoader();
  private static final String RESOURCE_FORMAT = "resource::%s:%s:%s:%s";

  @Nullable
  @Override
  public InputStream fetchResource(String s) {
    if (s.startsWith("/exchange_modules") || s.startsWith("exchange_modules")) {
      String[] resourceParts = s.split("/");
      int length = resourceParts.length;
      return resourceLoader.fetchResource(format(RESOURCE_FORMAT, resourceParts[length - 4], resourceParts[length - 3],
                                                 resourceParts[length - 2], resourceParts[length - 1]));
    }
    return resourceLoader.fetchResource(s);
  }
}
