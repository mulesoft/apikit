
package org.mule.module.apikit.rest.resource.document;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.AsbtractOperationTestCase;

public class RetrieveDocumentOperationTestCase extends AsbtractOperationTestCase
{

    @Override
    public AbstractRestOperation createOperation()
    {
        return new RetrieveDocumentOperation();
    }

}
