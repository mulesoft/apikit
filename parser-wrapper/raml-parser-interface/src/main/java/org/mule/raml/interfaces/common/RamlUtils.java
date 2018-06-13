/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.common;

import org.apache.commons.lang3.StringUtils;
import org.mule.raml.interfaces.model.IActionType;

public class RamlUtils {

  public static boolean isValidAction(String name) {
    for (IActionType actionType : IActionType.values()) {
      if (actionType.toString().equals(name.toUpperCase())) {
        return true;
      }
    }
    return false;
  }

  public static String replaceBaseUri(String raml, String newBaseUri) {
    if (newBaseUri != null) {
      return replaceBaseUri(raml, ".*$", newBaseUri);
    }
    return raml;
  }

  private static String replaceBaseUri(String raml, String regex, String replacement) {
    String[] split = raml.split("\n");
    boolean found = false;
    for (int i = 0; i < split.length; i++) {
      if (split[i].startsWith("baseUri: ")) {
        found = true;
        split[i] = split[i].replaceFirst(regex, replacement);
        if (!split[i].contains("baseUri: ")) {
          split[i] = "baseUri: " + split[i];
        }
      }
    }
    if (!found) {
      for (int i = 0; i < split.length; i++) {
        if (split[i].startsWith("title:")) {
          if (replacement.contains("baseUri:")) {
            split[i] = split[i] + "\n" + replacement;
          } else {
            split[i] = split[i] + "\n" + "baseUri: " + replacement;
          }
        }
      }
    }
    return StringUtils.join(split, "\n");
  }

}
