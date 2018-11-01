/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.mule.apikit.common.APISyncUtils;
import org.mule.module.apikit.metadata.MetadataTestCase;
import org.mule.module.apikit.metadata.api.ResourceLoader;

public class TestResourceLoader implements ResourceLoader {

  @Override
  public InputStream getResource(String relativePath) {
    try {
      if (APISyncUtils.isSyncProtocol(relativePath))
        relativePath = getTestPathForApiSync(relativePath);

      URL resource = MetadataTestCase.class.getResource(relativePath);
      if (resource == null)
        return null;
      final File file = new File(resource.toURI());

      return new FileInputStream(file);

    } catch (URISyntaxException | FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String getTestPathForApiSync(String relativePath) throws URISyntaxException {
    String[] parts = relativePath.split(":");


    if (parts.length < 8) {
      throw new URISyntaxException("APISync Resource loader doesn't recognize this path",
                                   "Not enough parts at url. Expected 8 actual " + parts.length);
    }

    String type = parts[5];
    String artifactId = parts[3];
    String groupId = parts[2];
    String fileName = parts[7];
    String version = parts[4];
    if (type.equals("raml"))
      return artifactId + "/" + fileName;

    if (type.equals("raml-fragment"))
      return groupId + "/exchange_modules/" + groupId + "/" + artifactId + "/" + version + "/" + fileName;

    throw new URISyntaxException("Resource loader doesn't recognize this path", "Invalid type " + type);
  }

}
