/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.loader;

import amf.client.remote.Content;
import amf.client.resource.ResourceLoader;
import org.mule.apikit.common.APISyncUtils;

import static org.mule.apikit.common.APISyncUtils.isExchangeModules;
import static org.mule.apikit.common.APISyncUtils.isSyncProtocol;
import java.util.concurrent.CompletableFuture;

public class ApiSyncResourceLoader implements ResourceLoader {

  private ResourceLoader resourceLoader;
  private String rootRamlResource;

  public ApiSyncResourceLoader(String rootRaml, ResourceLoader resourceLoader) {
    this.rootRamlResource = getRootRamlResource(rootRaml);
    this.resourceLoader = resourceLoader;
  }

  public ApiSyncResourceLoader(String rootRaml) {
    this(rootRaml, new ClassPathResourceLoader());
  }

  @Override
  public CompletableFuture<Content> fetch(String s) {
    CompletableFuture<Content> content = null;

    if (s.startsWith("/"))
      s = s.substring(1);

    if (isExchangeModules(s)) {
      content = getApiSyncResource(s);
    }

    if (content != null)
      return content;

    if (isSyncProtocol(s))
      return resourceLoader.fetch(s);

    return resourceLoader.fetch(rootRamlResource + s);
  }

  private String getRootRamlResource(String rootRamlResource) {
    return rootRamlResource.substring(0, rootRamlResource.lastIndexOf(":") + 1);
  }

  private CompletableFuture<Content> getApiSyncResource(String s) {
    String apiSyncResource = APISyncUtils.toApiSyncResource(s);
    if (apiSyncResource != null)
      return resourceLoader.fetch(apiSyncResource);
    return null;
  }
}
