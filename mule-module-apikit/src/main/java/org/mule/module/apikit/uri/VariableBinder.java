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
 * A convenience class is to bind variables to resolvers in a set of URI patterns.
 * <p/>
 * Variables can be bound to a resolver by type or by name.
 * <p/>
 * To assign a {@link VariableResolver} to variables of a specific type, use {@link #bindType(String, VariableResolver)}.
 * <p/>
 * The following example will bind all variables typed <code>int</code> to return the corresponding integer value.
 * <pre>
 *   VariableBinder binder = new VariableBinder();
 *   b.bindType("int", new VariableResolver(){
 *     public boolean exists(String v) {return v.matches("\\d+");}
 *     public Integer resolve(String v) {return exists(v)? Integer.valueOf(v) : null;};
 *   });
 * </pre>
 * <p/>
 * To assign a {@link VariableResolver} to variables of a specific name, use {@link #bindName(String, VariableResolver)}.
 * <p/>
 * The following example will bind all variables typed <code>int</code> to return the corresponding integer value.
 * <pre>
 *   VariableBinder binder = new VariableBinder();
 *   b.bindName("name", new VariableResolver(){
 *     public boolean exists(String v) {return true;}
 *     public Integer resolve(String v) {return exists(v)? Integer.valueOf(v) : null;};
 *   });
 * </pre>
 *
 * @author Christophe Lauret
 * @version 11 June 2009
 */
public class VariableBinder
{

    /**
     * The default resolver accepts everything and resolves all values to themselves.
     */
    private final static VariableResolver DEFAULT_RESOLVER = new VariableResolver()
    {
        public boolean exists(String value)
        {
            return true;
        }

        public Object resolve(String value)
        {
            return value;
        }
    };

    /**
     * Maps a variable names to a resolver.
     */
    private Map<String, VariableResolver> _byname = new Hashtable<String, VariableResolver>();

    /**
     * Maps a variable types to a resolver.
     */
    private Map<String, VariableResolver> _bytype = new Hashtable<String, VariableResolver>();

    /**
     * Binds the variables with the specified name to the specified resolver.
     *
     * @param name     The name of the variable.
     * @param resolver The resolver to use with these variables.
     * @deprecated use #bindName() or #bindType() instead
     */
    public void bind(String name, VariableResolver resolver)
    {
        this._byname.put(name, resolver);
    }

    /**
     * Binds the variables with the specified name to the specified resolver.
     *
     * @param name     The name of the variable.
     * @param resolver The resolver to use with these variables.
     */
    public void bindName(String name, VariableResolver resolver)
    {
        this._byname.put(name, resolver);
    }

    /**
     * Binds the variables with the specified name to the specified resolver.
     *
     * @param type     The variable type.
     * @param resolver The resolver to use with these variables.
     */
    public void bindType(String type, VariableResolver resolver)
    {
        this._bytype.put(type, resolver);
    }

    /**
     * Returns the resolver used for the variable of the specified name or type.
     * <p/>
     * <p>By default, looks for the resolver assigned to the specified variable name; if no resolver
     * is bound to the variable name, it will return the resolver bound to the given variable type.
     * <p/>
     * <p>This method does not return <code>null</code>. If the specified variable name or type is not
     * bound to any resolver the default resolver if returned instead.
     *
     * @param name The name of the variable.
     * @param type The type of the variable.
     * @return the corresponding resolver.
     */
    public VariableResolver getResolver(String name, VariableType type)
    {
        VariableResolver resolver = this._byname.get(name);
        // try to find a resolver by type
        if (resolver == null && type != null)
        {
            resolver = this._bytype.get(type.getName());
        }
        // fall back on the default otherwise
        return resolver != null ? resolver : DEFAULT_RESOLVER;
    }

    /**
     * Returns the resolver used for the variable of the specified name.
     * <p/>
     * <p>This method does not return <code>null</code>. If the specified variable name is no bound
     * to any resolver the default resolver if returned instead.
     *
     * @param name The name of the variables.
     * @return the corresponding resolver.
     */
    public VariableResolver getResolver(String name)
    {
        VariableResolver resolver = this._byname.get(name);
        return resolver != null ? resolver : DEFAULT_RESOLVER;
    }

    /**
     * Returns the resolver used for the variable of the specified type.
     * <p/>
     * <p>This method does not return <code>null</code>. If the specified variable name is no bound
     * to any resolver the default resolver if returned instead.
     *
     * @param type The type of the variable.
     * @return the corresponding resolver.
     */
    public VariableResolver getResolver(VariableType type)
    {
        if (type == null)
        {
            return DEFAULT_RESOLVER;
        }
        VariableResolver resolver = this._bytype.get(type.getName());
        return resolver != null ? resolver : DEFAULT_RESOLVER;
    }

    /**
     * Indicates whether the given variable name is bound to a VariableResolver.
     *
     * @param name The variable name.
     * @return <code>true</code> if a given variable resolver is bound to the specific name;
     *         <code>false</code> otherwise (including if the name is <code>null</code>.
     */
    public boolean isNameBound(String name)
    {
        if (name == null)
        {
            return false;
        }
        return this._byname.containsKey(name);
    }

    /**
     * Indicates whether the given variable type is bound to a VariableResolver.
     *
     * @param type The variable type.
     * @return <code>true</code> if a given variable resolver is bound to the specific type;
     *         <code>false</code> otherwise (including if the type is <code>null</code>.
     */
    public boolean isTypeBound(String type)
    {
        if (type == null)
        {
            return false;
        }
        return this._bytype.containsKey(type);
    }

}
