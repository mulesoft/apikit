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

package org.mule.webservice.rest.uri;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * A class to hold a collection of parameters for use during the expansion process.
 * 
 * It provides more convenient functions than the underlying map and handles the rules for parameter
 * values.
 * 
 * @author Christophe Lauret
 * @version 5 November 2009
 */
public class URIParameters implements Parameters {

  /**
   * Maps the parameter names to the values.
   */
  private Map<String, String[]> _parameters;

  /**
   * Creates a new instance.
   */
  public URIParameters() {
    this._parameters = new HashMap<String, String[]>();
  }

  /**
   * Creates a new instance from the specified map.
   * 
   * @param parameters The map of parameters to supply
   */
  public URIParameters(Map<String, String[]> parameters) {
    this._parameters = new HashMap<String, String[]>(parameters);
  }

  /**
   * {@inheritDoc}
   */
  public void set(String name, String value) {
    if (value == null) return;
    this._parameters.put(name, new String[] { value });
  }

  /**
   * {@inheritDoc}
   */
  public void set(String name, String[] values) {
    if (values == null) return;
    this._parameters.put(name, values);
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> names() {
    return Collections.unmodifiableSet(this._parameters.keySet());
  }

  /**
   * {@inheritDoc}
   */
  public String getValue(String name) {
    String[] vals = this._parameters.get(name);
    if (vals == null || vals.length == 0)
      return null;
    else
      return vals[0];
  }

  /**
   * {@inheritDoc}
   */
  public String[] getValues(String name) {
    return this._parameters.get(name);
  }

  /**
   * {@inheritDoc}
   */
  public boolean exists(String name) {
    return this._parameters.containsKey(name);
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasValue(String name) {
    String[] values = this._parameters.get(name);
    return values != null && values.length > 0 && values[0].length() > 0;
  }

}