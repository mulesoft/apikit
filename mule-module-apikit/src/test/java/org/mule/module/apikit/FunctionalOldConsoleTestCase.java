/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.tck.junit4.FunctionalTestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class FunctionalOldConsoleTestCase extends FunctionalTestCase
{
    @BeforeClass
    public static void setUp()
    {
        System.setProperty("apikit.console.old","true");
    }

    @AfterClass
    public static void tearDown()
    {
        System.clearProperty("apikit.console.old");
    }
}
