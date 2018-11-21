/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

public class VersionUtils {

  public static String getMaxVersion(String versionA, String versionB) {
    if (versionA == null || !versionA.matches("[0-9]+(\\.[0-9]+)*"))
      return versionB;

    String[] versionParts = versionA.split("\\.");
    String[] minVersionParts = versionB.split("\\.");

    if (versionParts.length != minVersionParts.length)
      return versionB;

    int length = minVersionParts.length;

    for (int i = 0; i < length; i++) {
      if (Integer.parseInt(versionParts[i]) < Integer.parseInt(minVersionParts[i]))
        return versionB;
    }

    return versionA;
  }
}
