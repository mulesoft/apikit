/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import java.util.Optional;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.module.apikit.metadata.internal.model.MetadataResolverFactory;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.core.api.util.StringUtils;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class RamlHandler implements MetadataResolverFactory {

  private final ResourceLoader resourceLoader;
  private final Notifier notifier;

  public RamlHandler(ResourceLoader resourceLoader, Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  @Override
  public Optional<MetadataResolver> getMetadataResolver(String apiDefinition) {
    return getApi(apiDefinition).map(raml -> new RamlApiWrapper(raml, notifier));
  }


  public Optional<IRaml> getApi(String uri) {
    try {

      if (StringUtils.isEmpty(uri)) {
        notifier.error("RAML document is undefined.");
        return empty();
      }

      final ParserService parserService = new ParserService(uri, resourceLoader);
      return of(parserService.build());

    } catch (Exception e) {
      notifier.error(format("Error reading RAML document '%s'. Detail: %s", uri, e.getMessage()));
    }

    return empty();
  }
}
