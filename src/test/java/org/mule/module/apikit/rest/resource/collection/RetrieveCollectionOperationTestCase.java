
package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.AsbtractOperationTestCase;

public class RetrieveCollectionOperationTestCase extends AsbtractOperationTestCase
{
    RetrieveCollectionOperation operation;

    @Override
    public AbstractRestOperation createOperation()
    {
        return new RetrieveCollectionOperation();
    }

}
