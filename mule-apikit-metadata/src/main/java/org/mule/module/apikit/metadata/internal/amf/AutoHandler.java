/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import amf.client.model.domain.WebApi;
import java.util.Optional;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.module.apikit.metadata.internal.model.MetadataResolverFactory;
import org.mule.module.apikit.metadata.internal.raml.RamlApiWrapper;
import org.mule.module.apikit.metadata.internal.raml.RamlHandler;
import org.mule.raml.interfaces.model.IRaml;

public class AutoHandler implements MetadataResolverFactory {

  private ResourceLoader resourceLoader;
  private final Notifier notifier;

  public AutoHandler(final ResourceLoader resourceLoader, final Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  @Override
  public Optional<MetadataResolver> getMetadataResolver(final String apiDefinition) {
    final AmfHandler amfHandler = new AmfHandler(resourceLoader, notifier);
    final Optional<WebApi> webApi = amfHandler.getApi(apiDefinition);
    if (webApi.isPresent())
      return webApi.map(api -> new AmfWrapper(api, notifier));

    // Fallback
    final RamlHandler ramlHandler = new RamlHandler(resourceLoader, notifier);
    final Optional<IRaml> iRaml = ramlHandler.getApi(apiDefinition);
    return iRaml.map(raml -> new RamlApiWrapper(raml, notifier));
  }
}
