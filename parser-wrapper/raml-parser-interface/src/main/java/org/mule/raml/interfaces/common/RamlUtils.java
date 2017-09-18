/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.common;

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
}
