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

package org.mule.module.wsapi.rest.uri;

import java.util.Set;

/**
 * Holds the values of a resolved variables.
 * 
 * @author Christophe Lauret
 * @version 27 May 2009
 */
public interface ResolvedVariables {

  /**
   * Returns the names of the variables which have been resolved.
   * 
   * @return The names of the variables which have been resolved.
   */
  public Set<String> names();

  /**
   * Returns the object corresponding to the specified variable name.
   * 
   * @param name The name of the variable.
   * 
   * @return The object corresponding to the specified variable; may be <code>null</code>.
   */
  public Object get(String name);

}