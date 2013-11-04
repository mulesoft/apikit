/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import java.util.List;


/**
 * Defines tokens which use an operator to handle one or more variables.
 *
 * @author Christophe Lauret
 * @version 9 February 2009
 */
public interface TokenOperator extends Token
{

    /**
     * Returns the list of variables used in this token.
     *
     * @return the list of variables.
     */
    List<Variable> variables();

}