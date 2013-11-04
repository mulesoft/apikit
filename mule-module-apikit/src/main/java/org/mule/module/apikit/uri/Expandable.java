/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.uri;


/**
 * A class implementing this interface can be expanded.
 *
 * @author Christophe Lauret
 * @version 30 December 2008
 */
public interface Expandable
{

    /**
     * Expands this object to produce a URI fragment as defined by the URI Template specifications.
     *
     * @param parameters The list of parameters and their values for substitution.
     * @return The expanded URI fragment
     */
    String expand(Parameters parameters);

}
