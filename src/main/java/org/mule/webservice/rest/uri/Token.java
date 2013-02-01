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

import java.util.Map;

/**
 * Defines a token in a URI pattern or template.
 * 
 * <p>All tokens can be represented as a text expression which cannot be
 * <code>null</code>.
 * 
 * <p>Two tokens having the same expression are considered equal.
 * 
 * @author Christophe Lauret
 * @version 30 December 2008
 */
public interface Token extends Expandable {

  /**
   * The expression corresponding to this token.
   * 
   * @return The expression corresponding to this token.
   */
  String expression();

  /**
   * Indicates whether this token can be resolved.
   * 
   * <p>A resolvable token contains variables which can be resolved.
   * 
   * @return <code>true</code> if variables can be resolved from the specified pattern;
   *         <code>false</code> otherwise.
   */
  boolean isResolvable();

  /**
   * Resolves the specified expanded URI part for this token.
   * 
   * <p>The resolution process requires all variables referenced in the token to be mapped to
   * the value that is present in the expanded URI data.
   * 
   * @param expanded The part of the URI that correspond to an expanded version of the token.
   * @param values   The variables mapped to their values as a result of resolution.
   * 
   * @return <code>true</code> this operation was successful; <code>false</code> otherwise.
   */
  public boolean resolve(String expanded, Map<Variable, Object> values);

}
