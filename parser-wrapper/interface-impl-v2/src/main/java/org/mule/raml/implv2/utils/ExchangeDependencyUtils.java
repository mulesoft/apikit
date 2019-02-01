/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExchangeDependencyUtils {

  private static final Pattern DEPENDENCY_PATH_PATTERN = Pattern.compile("^exchange_modules/|/exchange_modules/");

  private ExchangeDependencyUtils() {}

  public static String getEchangePath(String path) {
    final String resourceName;

    final Matcher matcher = DEPENDENCY_PATH_PATTERN.matcher(path);
    if (matcher.find()) {
      final int dependencyIndex = path.lastIndexOf(matcher.group(0));
      resourceName = dependencyIndex <= 0 ? path : path.substring(dependencyIndex);
    } else {
      resourceName = path;
    }
    return resourceName;
  }

  public static String getRootProjectPath(String path) {
    final Matcher matcher = DEPENDENCY_PATH_PATTERN.matcher(path);

    if (matcher.find()) {
      return path.substring(0, matcher.start());
    }

    return path;
  }

  public static boolean isExchangeModuleReference(String path) {
    final Matcher matcher = DEPENDENCY_PATH_PATTERN.matcher(path);
    return matcher.find();
  }
}
