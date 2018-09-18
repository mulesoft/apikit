/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import java.util.Optional;
import org.mule.module.apikit.metadata.interfaces.Notifier;
import org.mule.module.apikit.metadata.interfaces.ResourceLoader;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.core.api.util.StringUtils;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class RamlHandler {

  private final ResourceLoader resourceLoader;
  private final Notifier notifier;

  public RamlHandler(ResourceLoader resourceLoader, Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  public Optional<IRaml> getRamlApi(String uri) {
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
