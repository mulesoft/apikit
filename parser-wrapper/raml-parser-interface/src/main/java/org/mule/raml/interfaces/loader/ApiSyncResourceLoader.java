/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.loader;

import java.net.URI;

import static org.mule.raml.interfaces.common.APISyncUtils.isExchangeModules;
import static org.mule.raml.interfaces.common.APISyncUtils.isSyncProtocol;
import static org.mule.raml.interfaces.common.APISyncUtils.toApiSyncResource;

public class ApiSyncResourceLoader implements ResourceLoader {

  private ResourceLoader resourceLoader;
  private String rootRamlResource;

  public ApiSyncResourceLoader(String resource) {
    this(resource, new ClassPathResourceLoader());
  }

  public ApiSyncResourceLoader(String resource, ResourceLoader resourceLoader) {
    this.rootRamlResource = getRootRamlResource(resource);
    this.resourceLoader = resourceLoader;
  }

  @Override
  public URI getResource(String path) {
    final String resourcePath;
    if (path.startsWith("/"))
      resourcePath = path.substring(1);
    else
      resourcePath = path;

    if (isExchangeModules(resourcePath))
      return resourceLoader.getResource(toApiSyncResource(resourcePath));
    else if (isSyncProtocol(path))
      return resourceLoader.getResource(resourcePath);
    else
      return resourceLoader.getResource(rootRamlResource + resourcePath);
  }

  private String getRootRamlResource(String rootRamlResource) {
    return rootRamlResource.substring(0, rootRamlResource.lastIndexOf(":") + 1);
  }

}
