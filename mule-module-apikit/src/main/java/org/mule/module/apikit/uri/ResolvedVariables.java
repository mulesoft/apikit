/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.Set;

/**
 * Holds the values of a resolved variables.
 *
 * @author Christophe Lauret
 * @version 27 May 2009
 */
public interface ResolvedVariables
{

    /**
     * Returns the names of the variables which have been resolved.
     *
     * @return The names of the variables which have been resolved.
     */
    public Set<String> names();

    /**
     * Returns the object corresponding to the specified variable name.
     *
     * @param name The name of the variable.
     * @return The object corresponding to the specified variable; may be <code>null</code>.
     */
    public Object get(String name);

}