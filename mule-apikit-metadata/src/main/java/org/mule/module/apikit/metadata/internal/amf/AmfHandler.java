/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import amf.client.environment.DefaultEnvironment;
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
import amf.client.parse.Parser;
import java.io.File;
import java.net.URI;
import java.util.Optional;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.module.apikit.metadata.internal.model.MetadataResolverFactory;
import org.mule.runtime.core.api.util.StringUtils;

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
    try {

      if (StringUtils.isEmpty(apiDefinition)) {
        notifier.error("API definition is undefined using AMF parser.");
        return empty();
      }

      final URI uri = resourceLoader.getResource(apiDefinition);

      if (uri == null) {
        notifier.error(format("API definition '%s' not found using AMF parser.", apiDefinition));
        return empty();
      }

      final Parser parser = DocumentParser.getParserForApi(uri, DefaultEnvironment.apply());
      final Document document = DocumentParser.parseFile(parser, uri, true);
      final WebApi webApi = DocumentParser.getWebApi(document);
      notifier.info(format("Metadata for API definition '%s' was generated using AMF parser.", uri));

      return of(webApi);
    } catch (final ParserException e) {
      notifier.error(format("Error reading API definition '%s' using AMF parser. Detail: %s", apiDefinition, e.getMessage()));
    }
    return empty();
  }
}
