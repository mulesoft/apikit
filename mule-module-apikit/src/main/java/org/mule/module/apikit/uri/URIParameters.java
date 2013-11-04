/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * A class to hold a collection of parameters for use during the expansion process.
 * <p/>
 * It provides more convenient functions than the underlying map and handles the rules for parameter
 * values.
 *
 * @author Christophe Lauret
 * @version 5 November 2009
 */
public class URIParameters implements Parameters
{

    /**
     * Maps the parameter names to the values.
     */
    private Map<String, String[]> _parameters;

    /**
     * Creates a new instance.
     */
    public URIParameters()
    {
        this._parameters = new HashMap<String, String[]>();
    }

    /**
     * Creates a new instance from the specified map.
     *
     * @param parameters The map of parameters to supply
     */
    public URIParameters(Map<String, String[]> parameters)
    {
        this._parameters = new HashMap<String, String[]>(parameters);
    }

    /**
     * {@inheritDoc}
     */
    public void set(String name, String value)
    {
        if (value == null)
        {
            return;
        }
        this._parameters.put(name, new String[] {value});
    }

    /**
     * {@inheritDoc}
     */
    public void set(String name, String[] values)
    {
        if (values == null)
        {
            return;
        }
        this._parameters.put(name, values);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> names()
    {
        return Collections.unmodifiableSet(this._parameters.keySet());
    }

    /**
     * {@inheritDoc}
     */
    public String getValue(String name)
    {
        String[] vals = this._parameters.get(name);
        if (vals == null || vals.length == 0)
        {
            return null;
        }
        else
        {
            return vals[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] getValues(String name)
    {
        return this._parameters.get(name);
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists(String name)
    {
        return this._parameters.containsKey(name);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasValue(String name)
    {
        String[] values = this._parameters.get(name);
        return values != null && values.length > 0 && values[0].length() > 0;
    }

}