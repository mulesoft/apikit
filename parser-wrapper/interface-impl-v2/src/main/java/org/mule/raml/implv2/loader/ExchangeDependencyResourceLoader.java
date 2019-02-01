/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.loader;

import org.raml.v2.api.loader.ClassPathResourceLoader;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.FileResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.loader.UrlResourceLoader;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URI;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.mule.raml.implv2.utils.ExchangeDependencyUtils.getEchangePath;

public class ExchangeDependencyResourceLoader implements ResourceLoader {

  private static final String BASE_PATH = "api";
  private final URI basePath;
  private final ResourceLoader resourceLoader;

  public ExchangeDependencyResourceLoader() {
    this(BASE_PATH);
  }

  public ExchangeDependencyResourceLoader(String path) {
    basePath = getParent(URI.create(path.replace("\\", "/")));
    resourceLoader =
        new CompositeResourceLoader(new FileResourceLoader(path), new ClassPathResourceLoader(basePath.toString()),
                                    new UrlResourceLoader());
  }

  @Nullable
  @Override
  public InputStream fetchResource(String path) {
    if (isNullOrEmpty(path)) {
      return null;
    }

    final String resourceName = getEchangePath(path);

    return resourceLoader.fetchResource(basePath.resolve(resourceName).toString());
  }

  private URI getParent(URI uri) {
    return uri.toString().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
  }
}
