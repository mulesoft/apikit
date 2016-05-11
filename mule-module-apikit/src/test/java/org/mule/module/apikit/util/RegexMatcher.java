/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class RegexMatcher extends BaseMatcher
{

    private final String regex;

    public RegexMatcher(String regex)
    {
        this.regex = regex;
    }

    public boolean matches(Object o)
    {
        return ((String) o).matches(regex);
    }

    public void describeTo(Description description)
    {
        description.appendText("matches regex=" + regex);
    }

    public static RegexMatcher matchesPattern(String regex)
    {
        return new RegexMatcher(regex);
    }
}