/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.ArrayList;
import java.util.List;


/**
 * An abstract token for use as a base for other tokens.
 * <p/>
 * <p>This class is a base implementation of the {@link Token} interface.
 *
 * @author Christophe Lauret
 * @version 30 December 2008
 */
abstract class TokenBase implements Token
{

    /**
     * The expression for this token.
     */
    private final String _exp;

    /**
     * Creates a new expansion token.
     *
     * @param exp The expression corresponding to this URI token.
     * @throws NullPointerException If the specified expression is <code>null</code>.
     */
    public TokenBase(String exp) throws NullPointerException
    {
        if (exp == null)
        {
            throw new NullPointerException("Cannot create a token with a null value.");
        }
        this._exp = exp;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * By default a token is resolvable if it can be matched.
     */
    public boolean isResolvable()
    {
        return this instanceof Matchable;
    }

    /**
     * {@inheritDoc}
     */
    public String expression()
    {
        return this._exp;
    }

    /**
     * Two tokens are equals if and only if their string expression is equal.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        if ((o == null) || (o.getClass() != this.getClass()))
        {
            return false;
        }
        TokenBase t = (TokenBase) o;
        return (this._exp == t._exp || (_exp != null && _exp.equals(t._exp)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return 31 * this._exp.hashCode() + this._exp.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return this._exp;
    }

    // functions provided for convenience ---------------------------------------

    /**
     * Removes the curly brackets from the specified expression.
     * <p/>
     * If the expression is already stripped, this method returns the same string.
     *
     * @param The expression to 'strip'.
     * @return The raw expression (without the curly brackets).
     */
    protected final static String strip(String exp)
    {
        if (exp.length() < 2)
        {
            return exp;
        }
        if (exp.charAt(0) == '{' && exp.charAt(exp.length() - 1) == '}')
        {
            return exp.substring(1, exp.length() - 1);
        }
        else
        {
            return exp;
        }
    }

    /**
     * Returns the variables for a given expression containing a list of variables.
     *
     * @param exp An expression containing a comma separated list of variables.
     * @return A list of variables.
     * @throws URITemplateSyntaxException If thrown by the Variable parse method.
     */
    protected static final List<Variable> toVariables(String exp) throws URITemplateSyntaxException
    {
        String[] exps = exp.split(",");
        List<Variable> vars = new ArrayList<Variable>(exps.length);
        for (String e : exps)
        {
            vars.add(Variable.parse(e));
        }
        return vars;
    }

}
