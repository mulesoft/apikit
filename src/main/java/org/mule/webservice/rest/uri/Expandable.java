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
 * A class implementing this interface can be expanded.
 * 
 * @author Christophe Lauret
 * @version 30 December 2008
 */
public interface Expandable {

  /**
   * Expands this object to produce a URI fragment as defined by the URI Template specifications.
   * 
   * @param parameters The list of parameters and their values for substitution.
   * 
   * @return The expanded URI fragment
   */
  String expand(Parameters parameters);

}
