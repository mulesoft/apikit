/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import org.apache.commons.io.IOUtils;
import org.mule.module.apikit.metadata.interfaces.Notifier;
import org.mule.module.apikit.metadata.interfaces.Parseable;
import org.mule.module.apikit.metadata.interfaces.ResourceLoader;
import org.mule.raml.interfaces.model.IRaml;

import java.io.*;
import java.util.Optional;

import static java.lang.Boolean.getBoolean;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class RamlHandler {

  private static final String PARSER_V2_PROPERTY = "apikit.raml.parser.v2";

  private final ResourceLoader resourceLoader;
  private final Notifier notifier;

  public RamlHandler(ResourceLoader resourceLoader, Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  public Optional<IRaml> getRamlApi(String uri) {
    try {
      final File resource = resourceLoader.getRamlResource(uri);

      if (resource == null) {
        notifier.error(format("RAML document '%s' not found.", uri));
        return empty();
      }

      final String content = getRamlContent(resource);
      final Parseable parser = getParser(content);

      return of(parser.build(resource, content));
    } catch (IOException e) {
      notifier.error(format("Error reading RAML document '%s'. Detail: %s", uri, e.getMessage()));
    }

    return empty();
  }

  private Parseable getParser(String ramlContent) {
    return useParserV2(ramlContent) ? new RamlV2Parser() : new RamlV1Parser();
  }

  private String getRamlContent(File uri) throws IOException {
    try (final InputStream is = new FileInputStream(uri)) {
      return IOUtils.toString(is);
    }
  }

  private static boolean useParserV2(String content) {
    return getBoolean(PARSER_V2_PROPERTY) || content.startsWith("#%RAML 1.0");
  }
}
