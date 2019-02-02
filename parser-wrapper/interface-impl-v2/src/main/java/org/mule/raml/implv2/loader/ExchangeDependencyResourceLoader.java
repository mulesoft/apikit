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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.mule.raml.implv2.utils.ExchangeDependencyUtils.getExchangeModulePath;

public class ExchangeDependencyResourceLoader implements ResourceLoader {

  private final ResourceLoader resourceLoader;

  public ExchangeDependencyResourceLoader() {
    resourceLoader = new DefaultResourceLoader();
  }

  @Nullable
  @Override
  public InputStream fetchResource(String path) {
    if (isNullOrEmpty(path)) {
      return null;
    }

    return resourceLoader.fetchResource(getExchangeModulePath(path));
  }
}
