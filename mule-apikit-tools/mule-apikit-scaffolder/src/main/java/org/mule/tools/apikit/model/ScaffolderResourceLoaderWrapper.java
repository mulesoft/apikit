/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.apache.commons.io.FileUtils;
import org.raml.v2.api.loader.ResourceLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;

public class ScaffolderResourceLoaderWrapper implements ResourceLoader {

  ScaffolderResourceLoader scaffolderResourceLoader;

  public ScaffolderResourceLoaderWrapper(ScaffolderResourceLoader scaffolderResourceLoader) {
    this.scaffolderResourceLoader = scaffolderResourceLoader;
  }

  @Nullable
  @Override
  public InputStream fetchResource(String s) {
    return scaffolderResourceLoader.getResourceAsStream(s);
  }

  public File getFile(String resource) {
    return FileUtils.toFile(scaffolderResourceLoader.getResource(resource));
  }
}
