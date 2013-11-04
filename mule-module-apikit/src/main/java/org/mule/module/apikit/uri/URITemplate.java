/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A URI Template for constructing URIs following the same structure.
 * <p/>
 * Instances of this class implement the URI templates as defined by the URI Template (Draft 3) by
 * Joe Gregorio.
 * <p/>
 * A URI Template follows the URI syntax and can be expanded given a set of variable values.
 *
 * @author Christophe Lauret
 * @version 5 November 2009
 * @see <a
 *      href="http://bitworking.org/projects/URI-Templates/spec/draft-gregorio-uritemplate-03.html">URI
 *      Template (draft 3)</a>
 */
public class URITemplate implements Expandable
{

    /**
     * The regular expression pattern to identify template expansions within the template.
     */
    private final static Pattern EXPANSION_PATTERN = Pattern.compile("\\{[^}]*}");

    /**
     * The string representation of the URL template.
     */
    private final String _template;

    /**
     * The list of tokens corresponding to this URL template.
     */
    private final List<Token> _tokens;

    /**
     * Creates a new URI Template instance.
     *
     * @param template A String following the URI template syntax.
     * @throws NullPointerException       If the specified template is <code>null</code>.
     * @throws URITemplateSyntaxException If the string provided does not follow the proper syntax.
     */
    public URITemplate(String template) throws IllegalArgumentException
    {
        if (template == null)
        {
            throw new NullPointerException("Cannot create a URI template with a null template");
        }
        this._tokens = digest(template);
        this._template = template;
    }

    // TODO: method to indicate how many variables are used involved in the template

    /**
     * Creates a new URI Template instance using the specified token factory.
     * <p/>
     * <p>If the specified factory is <code>null</code>, the default is used.
     *
     * @param template A String following the URI template syntax.
     * @param factory  A token factory in order to choose the URI template syntax to use.
     * @throws NullPointerException       If the specified template is <code>null</code>.
     * @throws URITemplateSyntaxException If the string provided does not follow the proper syntax.
     */
    public URITemplate(String template, TokenFactory factory) throws IllegalArgumentException
    {
        if (template == null)
        {
            throw new NullPointerException("Cannot create a URI template with a null template");
        }
        this._tokens = digest(template, factory != null ? factory : TokenFactory.getInstance());
        this._template = template;
    }

    /**
     * Expands the template to produce a URI as defined by the URI Template specifications.
     *
     * @param parameters The list of variables and their values for substitution.
     */
    public String expand(Parameters parameters)
    {
        StringBuffer uri = new StringBuffer();
        for (Token t : this._tokens)
        {
            uri.append(t.expand(parameters));
        }
        return uri.toString();
    }

    /**
     * Method provided for convenience.
     * <p/>
     * It returns the same as:
     * <p/>
     * <pre>
     * return new URITemplate(template).expand(variables);
     * </pre>
     *
     * @param template   The URI template.
     * @param parameters The parameter values to use for substitution.
     * @return The corresponding expanded URI.
     */
    public static String expand(String template, Parameters parameters)
    {
        return new URITemplate(template).expand(parameters);
    }

    /**
     * Returns the list of tokens corresponding to the specified URI template.
     *
     * @param template The URI template to digest.
     * @return The corresponding list of URL tokens.
     * @throws URITemplateSyntaxException If the string cannot be parsed.
     */
    public static List<Token> digest(String template) throws URITemplateSyntaxException
    {
        return digest(template, TokenFactory.getInstance());
    }

    /**
     * Returns the list of tokens corresponding to the specified URI template.
     *
     * @param template The URI template to digest.
     * @return The corresponding list of URL tokens.
     * @throws URITemplateSyntaxException If the string cannot be parsed.
     */
    public static List<Token> digest(String template, TokenFactory factory) throws URITemplateSyntaxException
    {
        List<Token> tokens = new ArrayList<Token>();
        Matcher m = EXPANSION_PATTERN.matcher(template);
        int start = 0;
        while (m.find())
        {
            // any text since the last expansion
            if (m.start() > start)
            {
                String text = template.substring(start, m.start());
                tokens.add(new TokenLiteral(text));
            }
            // add the expansion
            String exp = m.group();
            tokens.add(factory.newToken(exp));
            // update the state variables
            start = m.end();
        }
        // any text left over, including if there were no expansions
        if (start < template.length())
        {
            String text = template.substring(start, template.length());
            // support for wild cards only at the end of the string.
            if (text.endsWith("*"))
            {
                tokens.add(new TokenLiteral(text.substring(0, text.length() - 1)));
                tokens.add(factory.newToken("*"));
            }
            else
            {
                tokens.add(new TokenLiteral(text));
            }
        }
        return tokens;
    }

    /**
     * Returns the underlying list of tokens.
     * <p/>
     * <p/>
     * Note: this method exposes the underlying structure of this class and should remain protected.
     *
     * @return The underlying list of tokens.
     */
    protected List<Token> tokens()
    {
        return this._tokens;
    }

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
        URITemplate t = (URITemplate) o;
        return (_template == t._template || (_template != null && _template.equals(t._template)));
    }

    @Override
    public int hashCode()
    {
        return 127 * this._template.hashCode() + this._template.hashCode();
    }

    @Override
    public String toString()
    {
        return this._template;
    }
}
