/**
 * Mule Rest Module
 *
 * Copyright 2011-2012 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package org.mule.module.apikit.rest.uri;

import java.util.Hashtable;
import java.util.Map;

/**
 * A variable resolver backed by a values mapped to objects.
 * 
 * @author Christophe Lauret
 * @version 30 December 2008
 */
public class VariableResolverMap<V> implements VariableResolver
{

  /**
   * The list of values.
   */
  private Map<String, ? extends V> _map;

  /**
   * Creates a new variable resolver.
   */
  public VariableResolverMap() {
    this._map = new Hashtable<String,V>();
  }

  /**
   * Creates a new variable resolver from the given map.
   * 
   * @param map Variable values mapped to objects.
   */
  public VariableResolverMap(Map<String,? extends V> map) {
    this._map = map;
  }

  /**
   * {@inheritDoc}
   */
  public boolean exists(String value) {
    if (value == null)
      return false;
    return this._map.containsKey(value);
  }

  /**
   * {@inheritDoc}
   */
  public V resolve(String value) {
    return this._map.get(value);
  }

}
