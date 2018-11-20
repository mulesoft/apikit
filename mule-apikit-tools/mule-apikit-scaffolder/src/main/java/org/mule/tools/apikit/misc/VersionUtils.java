/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

public class VersionUtils {

  public static String getMinVersion(String version, String minVersion) {
    if (version == null || !version.matches("[0-9]+(\\.[0-9]+)*"))
      return minVersion;

    String[] versionParts = version.split("\\.");
    String[] minVersionParts = minVersion.split("\\.");

    if (versionParts.length != minVersionParts.length)
      return minVersion;

    int length = minVersionParts.length;

    for (int i = 0; i < length; i++) {
      if (Integer.parseInt(versionParts[i]) < Integer.parseInt(minVersionParts[i]))
        return minVersion;
    }

    return version;
  }
}
