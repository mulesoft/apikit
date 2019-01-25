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

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExchangeDependencyResourceLoader implements ResourceLoader {

  private File workingDir = null;
  private final FileResourceLoader resourceLoader = new FileResourceLoader();

  private static final Pattern DEPENDENCY_PATH_PATTERN = Pattern.compile("^exchange_modules/|/exchange_modules/");

  public ExchangeDependencyResourceLoader(String rootDir) {
    String basePath = rootDir != null ? rootDir : ".";
    workingDir = new File(basePath);
  }

  @Override
  public CompletableFuture<Content> fetch(String path) {
    if (path == null || path.isEmpty())
      return fail();

    final Matcher matcher = DEPENDENCY_PATH_PATTERN.matcher(path);
    if (matcher.find()) {

      final int dependencyIndex = path.lastIndexOf(matcher.group(0));
      final String resourceName = dependencyIndex <= 0 ? path : path.substring(dependencyIndex);
      return resourceLoader.fetch(Paths.get(workingDir.getPath(), resourceName).toUri().toString());
    }
    return fail();
  }

  private CompletableFuture<Content> fail() {
    return CompletableFuture.supplyAsync(() -> {
      throw new RuntimeException("Failed to apply.");
    });
  }
}
