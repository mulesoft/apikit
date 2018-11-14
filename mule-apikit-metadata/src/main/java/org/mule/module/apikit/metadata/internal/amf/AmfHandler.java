/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import amf.client.environment.DefaultEnvironment;
import amf.client.environment.Environment;
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
import amf.client.parse.Parser;
import amf.client.remote.Content;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.io.IOUtils;
import org.mule.amf.impl.DocumentParser;
import org.mule.amf.impl.ParserWrapperAmf;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.apikit.common.APISyncUtils;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.module.apikit.metadata.internal.model.MetadataResolverFactory;
import org.mule.runtime.core.api.util.StringUtils;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mule.apikit.common.APISyncUtils.isExchangeModules;
import static org.mule.apikit.common.APISyncUtils.isSyncProtocol;

public class AmfHandler implements MetadataResolverFactory {

  private final ResourceLoader resourceLoader;
  private final Notifier notifier;

  public AmfHandler(final ResourceLoader resourceLoader, final Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  @Override
  public Optional<MetadataResolver> getMetadataResolver(final String apiDefinition) {
    return getApi(apiDefinition).map(webApi -> new AmfWrapper(webApi, notifier));
  }

  public Optional<WebApi> getApi(final String apiDefinition) {

    if (StringUtils.isEmpty(apiDefinition)) {
      notifier.error("API definition is undefined using AMF parser.");
      return empty();
    }

    final ParserWrapperAmf parserWrapper;

    try {
      if (APISyncUtils.isSyncProtocol(apiDefinition)) {
        Environment environment = DefaultEnvironment.apply();
        environment = environment.add(new ResourceLoaderWrapper(resourceLoader, apiDefinition));
        parserWrapper = ParserWrapperAmf.create(apiDefinition, environment, true);
      } else {

        final URI uri = resourceLoader.getResource(apiDefinition);
        if (uri == null) {
          notifier.error(format("API definition '%s' not found using AMF parser.", apiDefinition));
          return empty();
        }
        parserWrapper = ParserWrapperAmf.create(uri, true);
      }
    } catch (Exception e) {
      notifier.error(format("Error reading API definition '%s' using AMF parser. Detail: %s", apiDefinition, e.getMessage()));
      return empty();
    }
    notifier.info(format("Metadata for API definition '%s' was generated using AMF parser.", apiDefinition));
    return of(parserWrapper.getWebApi());
  }

  private static class ResourceLoaderWrapper implements amf.client.resource.ResourceLoader {

    private final String rootName;
    private final ResourceLoader resourceLoader;


    public ResourceLoaderWrapper(final ResourceLoader resourceLoader, final String apiDefinition) {
      this.resourceLoader = resourceLoader;
      this.rootName = APISyncUtils.getFileName(apiDefinition);
    }

    @Override
    public CompletableFuture<Content> fetch(String s) {
      CompletableFuture<Content> future = new CompletableFuture<>();

      try {
        final URI uri = resourceLoader.getResource(s);
        if (uri == null) {
          future.completeExceptionally(new Exception("Failed to apply."));
          return future;
        }
        final Content content = new Content(IOUtils.toString(uri), uri.toURL().toString());
        future.complete(content);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return future;
    }
  }

}
