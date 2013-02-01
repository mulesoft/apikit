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

/**
 * Classes implementing this interface should provide a mechanism to resolve the value of a
 * variable in the context of a URI pattern matching operation.
 * 
 * @author Christophe Lauret
 * @version 3 January 2009
 */
public interface VariableResolver {

  /**
   * Indicates whether the given value exists.
   * 
   * This method should return <code>true</code> only if the value can be resolved, that is 
   * <code>resolve(value) != null</code>.
   * 
   * @param value The value to check for existence.
   * 
   * @return <code>true</code> if the specified value can be resolved;
   *         <code>false</code> otherwise.
   */
  boolean exists(String value);

  /**
   * Resolves the variable and returns the associated object.
   * 
   * This method allows implementations to provide a lookup mechanism for variables if bound to
   * particular objects. 
   * 
   * It must not return <code>null</code> if the value a value exists, but should return 
   * <code>null</code>, if the value cannot be resolved.
   * 
   * If the implementation does not bind values to objects, this method should return the value if
   * it can be resolved otherwise, it should return <code>null</code>.
   * 
   * @param value The value to resolve.
   * 
   * @return Any associated object.
   */
  Object resolve(String value);

}
