/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.common;

import org.mule.raml.interfaces.model.ApiVendor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import static org.mule.raml.interfaces.model.ApiVendor.OAS;
import static org.mule.raml.interfaces.model.ApiVendor.OAS_20;
import static org.mule.raml.interfaces.model.ApiVendor.RAML;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_08;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_10;

public class ApiVendorUtils {

  private static final String SWAGGER = "SWAGGER";
  private static final String HEADER_RAML_10 = "#%RAML 1.0";
  private static final String HEADER_RAML_08 = "#%RAML 0.8";

  private ApiVendorUtils() {}

  public static ApiVendor deduceApiVendor(final InputStream is) {

    try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {

      String inputLine = getFirstLine(in);

      ApiVendor vendor = getRamlVendor(inputLine);
      if (vendor != null)
        return vendor;

      int lines = 0;
      do {
        if (inputLine.toUpperCase().contains(SWAGGER))
          return OAS_20;
        if (++lines == 10)
          break;
      } while ((inputLine = in.readLine()) != null);

    } catch (final IOException ignored) {
    }

    return RAML_10; // default value
  }

  public static ApiVendor getRamlVendor(InputStream inputStream) {
    ApiVendor vendor = null;

    try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
      final String header = getFirstLine(in);
      vendor = getRamlVendor(header);
    } catch (final IOException ignored) {
    }

    return vendor;
  }

  private static ApiVendor getRamlVendor(String header) {
    if (header.toUpperCase().startsWith(HEADER_RAML_08))
      return RAML_08;
    if (header.toUpperCase().startsWith(HEADER_RAML_10))
      return RAML_10;
    return null;
  }

  private static String getFirstLine(BufferedReader in) throws IOException {
    String line;
    while ((line = in.readLine()) != null) {
      if (line.trim().length() > 0)
        return line;
    }
    return "";
  }

}
