/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.Set;

/**
 * An interface to hold a collection of parameters for use during the expansion process.
 *
 * @author Christophe Lauret
 * @version 5 November 2009
 */
public interface Parameters
{

    /**
     * Returns the value for the specified parameter.
     *
     * @param name The name of the parameter.
     * @return The value for this parameter or <code>null</code> if not specified.
     */
    String getValue(String name);

    /**
     * Returns the values for the specified parameter.
     *
     * @param name The name of the parameter.
     * @return The values for this parameter or <code>null</code> if not specified.
     */
    String[] getValues(String name);

    /**
     * Indicates whether the parameters for the given name has a value.
     * <p/>
     * A parameter has a value if: - it is defined in the parameter list - its array of value has at
     * least one value that is not an empty string
     *
     * @param name The name of the parameter.
     * @return <code>true</code> if it has a value; <code>false</code> otherwise.
     */
    boolean exists(String name);

    /**
     * Indicates whether the parameters for the given name has a value.
     * <p/>
     * <p>A parameter has a value if:
     * <ul>
     * <li>It is defined in the parameter list</li>
     * <li>Its array of value has at least one value that is not an empty string</li>
     * </ul>
     *
     * @param name The name of the parameter.
     * @return <code>true</code> if it has a value;
     *         <code>false</code> otherwise.
     */
    boolean hasValue(String name);

    /**
     * Returns the set of parameter names as an unmodifiable set.
     *
     * @return The set of parameter names as an unmodifiable set.
     */
    Set<String> names();

    /**
     * Set a parameter with only one value.
     *
     * @param name  The name of the parameter.
     * @param value The value.
     */
    void set(String name, String value);

    /**
     * Set a parameter with only multiple values.
     *
     * @param name   The name of the parameter.
     * @param values The values.
     */
    void set(String name, String[] values);

}