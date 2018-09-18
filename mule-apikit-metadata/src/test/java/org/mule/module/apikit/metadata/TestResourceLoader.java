/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import org.mule.module.apikit.metadata.interfaces.ResourceLoader;

public class TestResourceLoader implements ResourceLoader {

  @Override
  public InputStream getRamlResource(String relativePath) {
    try {
      URL resource = this.getClass().getResource(relativePath);
      if (resource == null)
        return null;
      final File file = new File(resource.toURI());

      return new FileInputStream(file);

    } catch (URISyntaxException | FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

}
