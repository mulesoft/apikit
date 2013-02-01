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

import java.util.regex.Pattern;

/**
 * A class implementing this interface can be matched.
 * 
 * This interface can be used to indicate whether a class can be used for pattern matching.
 * 
 * @author Christophe Lauret
 * @version 4 February 2009
 */
public interface Matchable {

  /**
   * Indicates whether this token matches the specified part of a URL.
   * 
   * @param part The part of URL to test for matching.
   * 
   * @return <code>true</code> if it matches; <code>false</code> otherwise.
   */
  boolean match(String part);

  /**
   * Returns a regular expression pattern corresponding to this object.
   * 
   * @return The regular expression pattern corresponding to this object.
   */
  Pattern pattern();

}
