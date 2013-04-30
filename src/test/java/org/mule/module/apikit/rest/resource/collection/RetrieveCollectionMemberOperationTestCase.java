/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.AsbtractOperationTestCase;

public class RetrieveCollectionMemberOperationTestCase extends AsbtractOperationTestCase
{
    RetrieveCollectionMemberOperation operation;

    @Override
    public AbstractRestOperation createOperation()
    {
        return new RetrieveCollectionMemberOperation();
    }

}
