/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.loader;

import amf.client.remote.Content;
import org.apache.commons.io.IOUtils;
import org.mule.raml.interfaces.loader.ResourceLoader;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class ProvidedResourceLoader implements amf.client.resource.ResourceLoader {

  private ResourceLoader resourceLoader;

  public ProvidedResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public CompletableFuture<Content> fetch(String resourceName) {
    final CompletableFuture<Content> future = new CompletableFuture<>();

    if (resourceName == null || resourceName.isEmpty()) {
      throw new RuntimeException("Failed to apply.");
    }

    final URI resource = resourceLoader.getResource(resourceName);

    if (resource != null) {
      final String resourceAsString;
      try {
        resourceAsString = IOUtils.toString(resource.toURL().openStream());
        final Content content = new Content(resourceAsString, resource.toString());
        future.complete(content);
      } catch (IOException e) {
        future.completeExceptionally(new RuntimeException("Failed to fetch resource '" + resourceName + "'"));
      }
    } else {
      future.completeExceptionally(new Exception("Failed to fetch resource '" + resourceName + "'"));
    }

    return future;
  }

  private CompletableFuture<Content> fail() {
    return CompletableFuture.supplyAsync(() -> {
      throw new RuntimeException("Failed to apply.");
    });
  }
}
