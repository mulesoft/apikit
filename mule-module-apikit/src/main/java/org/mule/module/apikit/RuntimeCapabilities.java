/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.construct.Flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeCapabilities
{

    protected static final Logger logger = LoggerFactory.getLogger(RuntimeCapabilities.class);

    public static boolean supportsDinamicPipeline()
    {
        try
        {
            Flow.class.getMethod("dynamicPipeline", String.class);
        }
        catch (NoSuchMethodException e)
        {
            return false;
        }
        return true;
    }
}
