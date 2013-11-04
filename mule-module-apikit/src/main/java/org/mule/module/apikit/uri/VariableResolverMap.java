/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.Hashtable;
import java.util.Map;

/**
 * A variable resolver backed by a values mapped to objects.
 *
 * @author Christophe Lauret
 * @version 30 December 2008
 */
public class VariableResolverMap<V> implements VariableResolver
{

    /**
     * The list of values.
     */
    private Map<String, ? extends V> _map;

    /**
     * Creates a new variable resolver.
     */
    public VariableResolverMap()
    {
        this._map = new Hashtable<String, V>();
    }

    /**
     * Creates a new variable resolver from the given map.
     *
     * @param map Variable values mapped to objects.
     */
    public VariableResolverMap(Map<String, ? extends V> map)
    {
        this._map = map;
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists(String value)
    {
        if (value == null)
        {
            return false;
        }
        return this._map.containsKey(value);
    }

    /**
     * {@inheritDoc}
     */
    public V resolve(String value)
    {
        return this._map.get(value);
    }

}
