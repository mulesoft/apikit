/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

public class APISyncUtils {

  public static final String EXCHANGE_JSON = "exchange.json";
  public static final String API_SYNC_PROTOCOL = "resource::";
  public static final String RESOURCE_FORMAT = "resource::%s:%s:%s:%s:%s:%s";

  private APISyncUtils() {}

  public static boolean isSyncProtocol(final String path) {
    return path.startsWith(API_SYNC_PROTOCOL);
  }
}
