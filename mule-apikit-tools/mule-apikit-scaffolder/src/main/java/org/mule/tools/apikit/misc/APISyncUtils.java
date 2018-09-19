/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class APISyncUtils {

  public static final String EXCHANGE_JSON = "exchange.json";
  public static final String API_SYNC_PROTOCOL = "resource::";
  public static final String RESOURCE_FORMAT = "resource::%s:%s:%s:%s:%s:%s";

  final static String EXCHANGE_ROOT_RAML_TAG = "\"main\":\"";

  private APISyncUtils() {}

  public static String getRootRAMLFileName(InputStream exchangeJsonStream) {

    try {
      String exchangeJson = IOUtils.toString(exchangeJsonStream);
      exchangeJson = exchangeJson.substring(exchangeJson.indexOf(EXCHANGE_ROOT_RAML_TAG) + EXCHANGE_ROOT_RAML_TAG.length());
      exchangeJson = exchangeJson.substring(0, exchangeJson.indexOf("\""));
      return exchangeJson;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
