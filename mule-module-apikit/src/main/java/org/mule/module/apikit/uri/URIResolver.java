/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;


/**
 * A URI pattern resolver identifies the URI pattern and variables values given a specific URI.
 * <p/>
 * This class is typically used as follows:
 * <pre>
 *   // Create a resolver instance
 *   URIResolver resolver = new URIResolver("http://www.acme.com/test/home");
 *
 *   // Find the matching pattern amongst a list of precompiled URI patterns
 *   URIPattern pattern = resolver.find(patterns);
 *
 *   // Resolve the URI for the specified pattern, the result holds all the matching info
 *   URIResolveResult result = resolver.resolve(p);
 * </pre>
 *
 * @author Christophe Lauret
 * @version 11 June 2009
 */
public class URIResolver
{

    /**
     * Defines the priority rule for matching patterns.
     *
     * @author Christophe Lauret
     * @version 10 February 2009
     */
    public enum MatchRule
    {

        /**
         * Indicates that the first matching pattern should be returned when finding a match in a list
         * of patterns. The ordering of patterns in the list determines the matching pattern.
         */
        FIRST_MATCH,

        /**
         * Indicates that the best matching pattern should be returned when finding a match in a list
         * of patterns. The best matching patterns is determined by the longest common string.
         */
        BEST_MATCH
    }

    /**
     * The URI to resolve.
     */
    private String _uri;

    /**
     * Creates a new resolver for the specified URI.
     *
     * @param uri The URI to resolve.
     */
    public URIResolver(String uri, boolean encode)
    {
        if (encode)
        {
            uri = encode(uri);
        }
        this._uri = uri;
    }

    public URIResolver(String uri)
    {
        this(uri, true);
    }

    private String encode(String uri)
    {
        return URICoder.encode(uri, '/');
    }

    /**
     * Returns the URI handled by this resolver.
     *
     * @return The URI handled by this resolver.
     */
    public String uri()
    {
        return _uri;
    }

    /**
     * Returns the first URI pattern in the list which matches the underlying URI.
     *
     * @param patterns The URI patterns available.
     * @return The first URI pattern that matches or <code>null</code>.
     */
    public URIPattern find(Set<URIPattern> patterns)
    {
        return findFirst(patterns);
    }

    /**
     * Returns the first URI pattern in the list which matches the underlying URI.
     *
     * @param patterns The URI patterns available.
     * @param rule     The rule used to select the matching patterns in case of multiple matches.
     * @return The first URI pattern that matches or <code>null</code>.
     */
    public URIPattern find(Set<URIPattern> patterns, MatchRule rule)
    {
        switch (rule)
        {
            case FIRST_MATCH:
                return findFirst(patterns);
            case BEST_MATCH:
                return findBest(patterns);
        }
        return null;
    }

    /**
     * Returns all the URI patterns in the list which match the underlying URI.
     * <p/>
     * This methods returns an empty list if there are no matching patterns.
     *
     * @param patterns The URI patterns available.
     * @return A collection of matching URI patterns.
     */
    public Collection<URIPattern> findAll(Set<URIPattern> patterns)
    {
        Collection<URIPattern> matches = new ArrayList<URIPattern>();
        if (patterns == null || patterns.size() == 0)
        {
            return matches;
        }
        for (URIPattern p : patterns)
        {
            if (p.match(this._uri))
            {
                matches.add(p);
            }
        }
        return matches;
    }

    /**
     * Resolves the given URI pattern.
     *
     * @param pattern The pattern to resolve.
     * @return The URI pattern that best matches the given URI.
     */
    public URIResolveResult resolve(URIPattern pattern)
    {
        return resolve(pattern, new VariableBinder());
    }

    /**
     * Resolves the given URI pattern using the specified variable binder.
     *
     * @param pattern The pattern to resolve.
     * @param binder  The variable binder.
     * @return The URI pattern that best matches the given URI.
     */
    public URIResolveResult resolve(URIPattern pattern, VariableBinder binder)
    {
        URIResolveResult result = new URIResolveResult(pattern);
        Matcher mx = pattern.pattern().matcher(this._uri);
        boolean match = mx.matches();
        // it is an error condition if there is no match
        // or if the number of capturing groups is not the same as the number of tokens
        if (!match || mx.groupCount() != pattern.tokens().size())
        {
            result.setStatus(URIResolveResult.Status.ERROR);
            return result;
        }
        // extracts the variable token
        List<Token> tokens = pattern.tokens();
        Map<Variable, Object> map = new HashMap<Variable, Object>();
        for (int i = 0; i < mx.groupCount(); i++)
        {
            Token mt = tokens.get(i);
            String s = mx.group(i + 1);
            mt.resolve(s, map);
        }
        // lookup variable values
        lookup(result, map, binder);
        return result;
    }

    // private helpers ----------------------------------------------------------

    /**
     * Lookup the variable values using the variable resolvers specified in the bindings from the
     * values mapped to the variables.
     * <p/>
     * Set the status of the result accordingly.
     *
     * @param result Where the results go.
     * @param map    Values mapped to the variables.
     * @param binder The resolvers to use for each variable.
     */
    private void lookup(URIResolveResult result, Map<Variable, Object> map, VariableBinder binder)
    {
        URIResolveResult.Status status = URIResolveResult.Status.RESOLVED;
        // lookup variable values
        for (Map.Entry<Variable, Object> entry : map.entrySet())
        {
            Variable var = entry.getKey();
            VariableResolver r = binder.getResolver(var.name(), var.type());
            Object value = entry.getValue();

            // most common case: a string
            if (value instanceof String)
            {
                Object o = r.resolve(value.toString());
                result.put(entry.getKey().name(), o);
                if (o == null)
                {
                    status = URIResolveResult.Status.UNRESOLVED;
                }

                // returned an array of values
            }
            else if (value instanceof String[])
            {
                // FIXME: handle arrays
                status = URIResolveResult.Status.ERROR;

                // unknown object
            }
            else
            {
                status = URIResolveResult.Status.ERROR;
            }
        }
        result.setStatus(status);
    }

    /**
     * Returns the first URI pattern in the list which matches the underlying URI.
     *
     * @param patterns The URI patterns available.
     * @return The first URI pattern that matches or <code>null</code>.
     */
    private URIPattern findFirst(Set<URIPattern> patterns)
    {
        if (patterns == null || patterns.size() == 0)
        {
            return null;
        }
        for (URIPattern p : patterns)
        {
            if (p.match(this._uri))
            {
                return p;
            }
        }
        return null;
    }

    /**
     * Returns the best URI pattern in the list which matches the underlying URI.
     *
     * @param patterns The URI patterns available.
     * @return The best URI pattern that matches or <code>null</code>.
     */
    private URIPattern findBest(Set<URIPattern> patterns)
    {
        if (patterns == null || patterns.size() == 0)
        {
            return null;
        }
        URIPattern best = null;
        for (URIPattern p : patterns)
        {
            if (p.match(this._uri))
            {
                if (best == null || p.score() > best.score())
                {
                    best = p;
                }
            }
        }
        return best;
    }

}
