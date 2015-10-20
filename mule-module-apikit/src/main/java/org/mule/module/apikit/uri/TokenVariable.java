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
 * A URI token wrapping a variable.
 * <p/>
 * <p>Variables follow the following expression:
 * <pre>
 * var         = varname [ &quot;=&quot; vardefault ]
 * varname     = (ALPHA / DIGIT)*(ALPHA / DIGIT / &quot;.&quot; / &quot;_&quot; / &quot;-&quot; )
 * vardefault  = *(unreserved / pct-encoded)
 * </pre>
 *
 * @author Christophe Lauret
 * @version 30 December 2008
 */
public class TokenVariable extends TokenBase implements Token, Matchable
{

    /**
     * The variable for this token.
     */
    private Variable _var;

    /**
     * Creates a new variable token.
     *
     * @param exp The expression to create a new.
     * @throws NullPointerException       If the specified expression is <code>null</code>.
     * @throws URITemplateSyntaxException If the specified expression could not be parsed as a
     *                                    variable.
     */
    public TokenVariable(String exp) throws NullPointerException, URITemplateSyntaxException
    {
        this(Variable.parse(exp));
    }

    /**
     * Creates a new variable token.
     *
     * @param var The variable this token corresponds to.
     * @throws NullPointerException If the specified text is <code>null</code>.
     */
    public TokenVariable(Variable var) throws NullPointerException
    {
        super('{' + var.toString() + "}");
        this._var = var;
    }

    /**
     * Returns the variable wrapped by this token.
     *
     * @return The variable wrapped by this token.
     */
    public Variable getVariable()
    {
        return _var;
    }

    /**
     * {@inheritDoc}
     */
    public String expand(Parameters variables)
    {
        return URICoder.encode(this._var.value(variables));
    }

    /**
     * {@inheritDoc}
     */
    public boolean match(String value)
    {
        return Variable.isValidValue(value);
    }

    /**
     * {@inheritDoc}
     */
    public Pattern pattern()
    {
        return Variable.VALID_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    public boolean resolve(String expanded, Map<Variable, Object> values)
    {
        values.put(this._var, URICoder.decode(expanded).replaceAll("(?i)%2F", "/"));
        return true;
    }

}
