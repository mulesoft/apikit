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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Holds the results of a URI resolver.
 * 
 * @author Christophe Lauret
 * @version 5 February 2010
 */
public class URIResolveResult implements ResolvedVariables {

  /**
   * The possible status of a resolve result.
   */
  public enum Status {UNRESOLVED, RESOLVED, ERROR}

  /**
   * The status of this result.
   */
  private Status _status = Status.UNRESOLVED;

  /**
   * Maps variable names to their corresponding resolved objects.
   */
  private final Map<String, Object> values = new HashMap<String, Object>();

  /**
   * The URI Pattern that was used to produce this result.
   */
  private final URIPattern _pattern;

  /**
   * Constructs an instance of this class with fields initialised to null.
   */
  protected URIResolveResult(URIPattern pattern) {
    this._pattern = pattern;
  }

  /** 
   * {@inheritDoc}
   */
  public Set<String> names() {
    return this.values.keySet();
  }

  /**
   * {@inheritDoc}
   */
  public Object get(String name) {
    return this.values.get(name);
  }

  /**
   * Returns the status of this result.
   * 
   * @return The status of this result.
   */
  public Status getStatus() {
    return this._status;
  }

  /**
   * Returns the URI Pattern that was used to produce this result.
   * 
   * @return The URI Pattern that was used to produce this result.
   */
  public URIPattern getURIPattern() {
    return this._pattern;
  }

// protected methods --------------------------------------------------------

  /**
   * Puts the object corresponding to the specified variable name in the results.
   * 
   * @param name The name of the variable.
   * @param o    The corresponding object.
   */
  protected void put(String name, Object o) {
    this.values.put(name, o);
  }

  /**
   * Sets the status of this result.
   * 
   * @param status The status of the result.
   */
  protected void setStatus(Status status) {
    this._status = status;
  }

}
