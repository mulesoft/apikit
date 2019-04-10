/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.apache.commons.io.FileUtils;
import org.mule.raml.interfaces.common.APISyncUtils;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;

public class TestScaffolderResourceLoader implements ScaffolderResourceLoader {

  private String ramlFolder;

  public TestScaffolderResourceLoader(String ramlFolder) {
    this.ramlFolder = ramlFolder;
  }

  @Override
  public InputStream getResourceAsStream(String resource) {
    File file = getFile(resource);
    if (file.exists()) {
      try {
        return FileUtils.openInputStream(file);
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }

    return null;
  }

  @Override
  public URI getResource(String resource) {
    File file = getFile(resource);
    if (file.exists())
      return file.toURI();
    return null;
  }

  private File getFile(String resource) {
    String file = resource;

    if (APISyncUtils.isSyncProtocol(resource)) {
      file = resource.substring(resource.lastIndexOf(":") + 1);
    }

    return new File(ramlFolder + "/" + file);
  }
}
