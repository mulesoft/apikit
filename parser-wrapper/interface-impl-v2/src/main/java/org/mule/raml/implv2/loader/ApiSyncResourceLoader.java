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

  public static final String EXCHANGE_MODULES = "exchange_modules";
  private ResourceLoader resourceLoader;
  private static final String RESOURCE_FORMAT = "resource::%s:%s:%s:%s:%s:%s";
  private static final String RAML_FRAGMENT_CLASSIFIER = "raml-fragment";
  private static final String EXCHANGE_TYPE = "zip";

  public static final String API_SYNC_PROTOCOL = "resource::";

  private String rootRamlResource;

  public ApiSyncResourceLoader(String rootRaml) {
    this(rootRaml, new DefaultResourceLoader());
  }

  public ApiSyncResourceLoader(String rootRaml, ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
    this.rootRamlResource = getRootRamlResource(rootRaml);
  }

  private String getRootRamlResource(String rootRamlResource) {
    return rootRamlResource.substring(0, rootRamlResource.lastIndexOf(":") + 1);
  }


  @Nullable
  @Override
  public InputStream fetchResource(String s) {
    InputStream stream = null;

    if (s.startsWith("/"))
      s = s.substring(1);

    if (s.startsWith(EXCHANGE_MODULES)) {
      stream = getApiSyncResource(s);
    }

    if (stream != null)
      return stream;

    if (s.startsWith(API_SYNC_PROTOCOL))
      return resourceLoader.fetchResource(s);

    return resourceLoader.fetchResource(rootRamlResource + s);
  }

  private InputStream getApiSyncResource(String s) {
    InputStream stream = null;
    String[] resourceParts = s.split("/");
    int length = resourceParts.length;
    if (length > 4)
      stream = resourceLoader.fetchResource(format(RESOURCE_FORMAT, resourceParts[length - 4], resourceParts[length - 3],
                                                   resourceParts[length - 2], RAML_FRAGMENT_CLASSIFIER,
                                                   EXCHANGE_TYPE, resourceParts[length - 1]));
    return stream;
  }
}
