/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.io.InputStream;
import java.net.URL;

public interface ScaffolderResourceLoader {

  /**
   * Returns an {@link InputStream} with the resource's data ready to be consumed.
   *
   * @param resource the resource to be found.
   * @return a stream to read data from the resource.
   */
  InputStream getResourceAsStream(String resource);

  /**
   * Returns the resources {@link URL}, useful when the actual resource will be loaded by another component.
   *
   * @param resource the resource to be found.
   * @return A URL pointing to the resource.
   */
  URL getResource(String resource);
}
