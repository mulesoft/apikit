/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.apache.commons.io.FileUtils;
import org.mule.tools.apikit.misc.APISyncUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;

import static java.lang.String.format;

public class ScaffolderResourceLoaderWrapper
    implements org.raml.v2.api.loader.ResourceLoader, org.raml.parser.loader.ResourceLoader {

  private final String rootRamlResource;
  ScaffolderResourceLoader scaffolderResourceLoader;
  private static final String RAML_FRAGMENT_CLASSIFIER = "raml-fragment";
  private static final String EXCHANGE_TYPE = "zip";
  public static final String EXCHANGE_MODULES = "exchange_modules";

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

    if (s.startsWith(EXCHANGE_MODULES)) {
      stream = scaffolderResourceLoader.getResourceAsStream(s);
    }

    if (stream != null)
      return stream;

    if (s.startsWith(APISyncUtils.API_SYNC_PROTOCOL))
      return scaffolderResourceLoader.getResourceAsStream(s);

    return scaffolderResourceLoader.getResourceAsStream(rootRamlResource + s);
  }



  public File getFile(String resource) {
    return FileUtils.toFile(scaffolderResourceLoader.getResource(resource));
  }
}
