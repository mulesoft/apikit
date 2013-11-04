/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.Map;
import java.util.regex.Pattern;


/**
 * A URI token corresponding to the literal text part of the URI template.
 * <p/>
 * <p>Literal text remains identical during the expansion process (parameters are ignored).
 * <p/>
 * <p>Literal text tokens only match text that is equal.
 * <p/>
 * <p>The expression for a literal token does contain curly brackets.
 *
 * @author Christophe Lauret
 * @version 9 February 2009
 */
public class TokenLiteral extends TokenBase implements Token, Matchable
{

    /**
     * Creates a new literal text token.
     *
     * @param text The text corresponding to this URI token.
     * @throws NullPointerException If the specified text is <code>null</code>.
     */
    public TokenLiteral(String text) throws NullPointerException
    {
        super(text);
    }

    /**
     * {@inheritDoc}
     */
    public String expand(Parameters parameters)
    {
        return this.expression();
    }

    /**
     * {@inheritDoc}
     */
    public boolean match(String part)
    {
        return this.expression().equals(part);
    }

    /**
     * {@inheritDoc}
     */
    public Pattern pattern()
    {
        return Pattern.compile(Pattern.quote(expression()));
    }

    /**
     * {@inheritDoc}
     * <p/>
     * By definition, no variable in this token. This method does nothing and always
     * returns <code>true</code>.
     */
    public boolean resolve(String expanded, Map<Variable, Object> values)
    {
        // nothing to resolve - the operation is always successful.
        return true;
    }

}
