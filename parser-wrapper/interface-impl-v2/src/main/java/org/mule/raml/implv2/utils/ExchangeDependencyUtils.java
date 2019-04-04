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

  public static String getExchangeModulePath(String path) {
    final Matcher matcher = DEPENDENCY_PATH_PATTERN.matcher(path);
    if (matcher.find()) {
      final String matching = matcher.group(0);
      final int dependencyIndex = path.lastIndexOf(matching);

      if (dependencyIndex <= 0)
        return path;
      else {
        final String rootPath = path.substring(0, path.indexOf(matching));
        final String exchangeModulePath = path.substring(dependencyIndex);
        return rootPath + "/" + exchangeModulePath;
      }
    } else {
      return path;
    }
  }

  public static boolean isExchangeModuleReference(String path) {
    final Matcher matcher = DEPENDENCY_PATH_PATTERN.matcher(path);
    return matcher.find();
  }
}
