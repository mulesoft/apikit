/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import amf.client.remote.Content;
import amf.client.resource.FileResourceLoader;
import amf.client.resource.ResourceLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import static org.mule.apikit.common.APISyncUtils.isSyncProtocol;
import static org.mule.apikit.common.APISyncUtils.isExchangeModules;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;


public class ScaffolderResourceLoaderWrapper
    implements org.raml.v2.api.loader.ResourceLoader, org.raml.parser.loader.ResourceLoader, ResourceLoader {

  private final String rootRamlResource;
  ScaffolderResourceLoader scaffolderResourceLoader;


  public ScaffolderResourceLoaderWrapper(ScaffolderResourceLoader scaffolderResourceLoader, String rootRamlName) {
    this.scaffolderResourceLoader = scaffolderResourceLoader;
    this.rootRamlResource = getRootRamlResource(rootRamlName);
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

    if (isExchangeModules(s)) {
      stream = scaffolderResourceLoader.getResourceAsStream(s);
    }

    if (stream != null)
      return stream;

    if (isSyncProtocol(s))
      return scaffolderResourceLoader.getResourceAsStream(s);

    return scaffolderResourceLoader.getResourceAsStream(rootRamlResource + s);
  }



  public File getFile(String resource) {
    return FileUtils.toFile(scaffolderResourceLoader.getResource(resource));
  }

  @Override
  public CompletableFuture<Content> fetch(String s) {
    CompletableFuture<Content> future = new CompletableFuture<>();

    if (s.startsWith("/"))
      s = s.substring(1);


    if (!(isSyncProtocol(s) || isExchangeModules(s))) {
      s = rootRamlResource + s;
    }
    if (s == null || s.isEmpty()) {
      future.completeExceptionally(new Exception("Failed to apply."));
      return future;
    }

    try {
      Content content =
          new Content(IOUtils.toString(scaffolderResourceLoader.getResourceAsStream(s)),
                      scaffolderResourceLoader.getResource(s).toURI().toURL().toString());
      future.complete(content);
    } catch (Exception e) {
      e.printStackTrace();
    }


    return future;

  }

  private CompletableFuture<Content> fail() {
    return CompletableFuture.supplyAsync(() -> {
      throw new RuntimeException("Failed to apply.");
    });
  }
}
