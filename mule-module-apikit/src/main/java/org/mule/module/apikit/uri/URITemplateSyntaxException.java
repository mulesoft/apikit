/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

/**
 * Thrown to indicate that a URI Template or URI Template fragment does not follow the appropriate
 * syntax.
 * <p/>
 * <p/>
 * This exception would typically be used for errors when parsing an expression supposed to follow
 * the URI template syntax.
 *
 * @author Christophe Lauret
 * @version 31 December 2008
 */
public class URITemplateSyntaxException extends IllegalArgumentException
{

    /**
     * For serialisation.
     */
    private static final long serialVersionUID = -8924504091165837799L;

    /**
     * The input string.
     */
    private final String _input;

    /**
     * The reason string.
     */
    private final String _reason;

    /**
     * Constructs an instance from the given input string, reason.
     *
     * @param input  The input string.
     * @param reason A string explaining why the input could not be parsed.
     */
    public URITemplateSyntaxException(String input, String reason)
    {
        super(reason + " : " + input);
        if ((input == null) || (reason == null))
        {
            throw new NullPointerException();
        }
        this._input = input;
        this._reason = reason;
    }

    /**
     * Returns the input string.
     *
     * @return The input string.
     */
    public String getInput()
    {
        return this._input;
    }

    /**
     * Returns the reason explaining why the input string could not be parsed.
     *
     * @return The reason string.
     */
    public String getReason()
    {
        return this._reason;
    }
}
