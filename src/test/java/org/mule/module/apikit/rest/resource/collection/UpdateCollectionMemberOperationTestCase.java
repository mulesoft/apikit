
package org.mule.module.apikit.rest.resource.collection;

import org.mule.api.MuleException;
import org.mule.module.apikit.rest.operation.AsbtractOperationTestCase;
import org.mule.module.apikit.rest.operation.RestOperation;

import org.junit.Before;

public class UpdateCollectionMemberOperationTestCase extends AsbtractOperationTestCase
{
    UpdateCollectionMemberOperation operation;

    @Override
    public RestOperation getOperation()
    {
        return operation;
    }

    @Before
    public void setup() throws MuleException
    {
        super.setup();
        operation = new UpdateCollectionMemberOperation();
        operation.setHandler(handler);
    }

}
