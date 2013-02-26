
package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.AsbtractOperationTestCase;

public class DeleteCollectionMemberOperationTestCase extends AsbtractOperationTestCase
{
    @Override
    public AbstractRestOperation createOperation()
    {
        return new DeleteCollectionMemberOperation();
    }

}
