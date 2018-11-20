/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import amf.client.model.domain.WebApi;
import amf.client.remote.Content;
import org.apache.commons.io.IOUtils;
import org.mule.amf.impl.ParserWrapperAmf;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.module.apikit.metadata.internal.model.MetadataResolverFactory;
import org.mule.raml.interfaces.common.APISyncUtils;
import org.mule.raml.interfaces.model.api.ApiRef;
import org.mule.runtime.core.api.util.StringUtils;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

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
      final ApiRef apiRef = ApiRef.create(apiDefinition, resourceLoader);;
      parserWrapper = ParserWrapperAmf.create(apiRef, true);
    } catch (Exception e) {
      notifier.error(format("Error reading API definition '%s' using AMF parser. Detail: %s", apiDefinition, e.getMessage()));
      return empty();
    }
    notifier.info(format("Metadata for API definition '%s' was generated using AMF parser.", apiDefinition));
    return of(parserWrapper.getWebApi());
  }

}
