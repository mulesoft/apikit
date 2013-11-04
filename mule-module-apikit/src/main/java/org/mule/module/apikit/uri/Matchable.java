/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.regex.Pattern;

/**
 * A class implementing this interface can be matched.
 * <p/>
 * This interface can be used to indicate whether a class can be used for pattern matching.
 *
 * @author Christophe Lauret
 * @version 4 February 2009
 */
public interface Matchable
{

    /**
     * Indicates whether this token matches the specified part of a URL.
     *
     * @param part The part of URL to test for matching.
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
