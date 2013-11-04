/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

/**
 * Classes implementing this interface should provide a mechanism to resolve the value of a
 * variable in the context of a URI pattern matching operation.
 *
 * @author Christophe Lauret
 * @version 3 January 2009
 */
public interface VariableResolver
{

    /**
     * Indicates whether the given value exists.
     * <p/>
     * This method should return <code>true</code> only if the value can be resolved, that is
     * <code>resolve(value) != null</code>.
     *
     * @param value The value to check for existence.
     * @return <code>true</code> if the specified value can be resolved;
     *         <code>false</code> otherwise.
     */
    boolean exists(String value);

    /**
     * Resolves the variable and returns the associated object.
     * <p/>
     * This method allows implementations to provide a lookup mechanism for variables if bound to
     * particular objects.
     * <p/>
     * It must not return <code>null</code> if the value a value exists, but should return
     * <code>null</code>, if the value cannot be resolved.
     * <p/>
     * If the implementation does not bind values to objects, this method should return the value if
     * it can be resolved otherwise, it should return <code>null</code>.
     *
     * @param value The value to resolve.
     * @return Any associated object.
     */
    Object resolve(String value);

}
