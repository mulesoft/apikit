/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model.api;

import org.mule.raml.interfaces.common.APISyncUtils;
import org.mule.raml.interfaces.loader.ResourceLoader;
import org.mule.raml.interfaces.model.ApiVendor;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.mule.raml.interfaces.common.ApiVendorUtils.deduceApiVendor;
import static org.mule.raml.interfaces.common.ApiVendorUtils.getRamlVendor;
import static org.mule.raml.interfaces.model.ApiVendor.OAS_20;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_10;

public interface ApiRef {

  static ApiRef create(final String location, final ResourceLoader resourceLoader) {
    if (APISyncUtils.isSyncProtocol(location))
      return resourceLoader != null ? new ApiSyncApiRef(location, resourceLoader) : new ApiSyncApiRef(location);

    try {
      final URI uri = new URI(location);
      if (uri.isAbsolute())
        return new URIApiRef(uri, resourceLoader);
    } catch (URISyntaxException ignored) {
    }

    // File is the default implementation
    return new DefaultApiRef(location, resourceLoader);
  }

  static ApiRef create(final String location) {
    return create(location, null);
  }

  static ApiRef create(final URI uri) {
    return new URIApiRef(uri);
  }

  String getLocation();

  String getFormat();

  InputStream resolve();

  default ApiVendor getVendor() {
    final String format = getFormat();

    if ("RAML".equalsIgnoreCase(format)) {
      final ApiVendor ramlVendor = getRamlVendor(resolve());
      return ramlVendor != null ? ramlVendor : RAML_10;
    }

    if ("JSON".equalsIgnoreCase(format))
      return OAS_20;

    return deduceApiVendor(resolve());
  }

  Optional<ResourceLoader> getResourceLoader();
}
