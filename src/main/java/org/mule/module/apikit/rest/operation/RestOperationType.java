/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.operation;

public enum RestOperationType
{
    CREATE
    {
        @Override
        public boolean isRequestExpected()
        {
            return true;
        }
    },
    RETRIEVE
    {
        @Override
        public boolean isResponseExpected()
        {
            return true;
        }
    },
    UPDATE
    {
        @Override
        public boolean isRequestExpected()
        {
            return true;
        }
    },
    DELETE, EXISTS;

    public boolean isRequestExpected()
    {
        return false;
    }

    public boolean isResponseExpected()
    {
        return false;
    }

}
