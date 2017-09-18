/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtils {

  private CollectionUtils() {}

  public static <K, V> Map<K, V> merge(List<Map<K, V>> maps) {
    final Map<K, V> map = new HashMap<>();

    maps.forEach(map::putAll);

    return map;
  }

}
