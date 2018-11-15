/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.loader;

import amf.client.remote.Content;
import amf.client.resource.FileResourceLoader;
import amf.client.resource.ResourceLoader;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ClassPathResourceLoader implements ResourceLoader {

  @Override
  public CompletableFuture<Content> fetch(String resourceName) {

    CompletableFuture<Content> future = new CompletableFuture<>();
    if (resourceName == null || resourceName.isEmpty()) {
      future.completeExceptionally(new Exception("Failed to apply."));
      return future;
    }

    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    final URL resource = contextClassLoader.getResource(resourceName);

    if (resource != null) {
      final String resourceAsString;
      try {
        resourceAsString = IOUtils.toString(resource.openStream());
        final Content content = new Content(resourceAsString, resource.toString());
        future.complete(content);
      } catch (IOException e) {
        future.completeExceptionally(new Exception("Failed to fetch resource '" + resourceName + "'"));
      }
    }

    return future;
  }


  private CompletableFuture<Content> fail() {
    return CompletableFuture.supplyAsync(() -> {
      throw new RuntimeException("Failed to apply.");
    });
  }

}
