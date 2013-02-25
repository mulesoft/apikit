
package org.mule.module.apikit.rest.resource.document;

import org.mule.api.MuleException;
import org.mule.module.apikit.rest.operation.AsbtractOperationTestCase;
import org.mule.module.apikit.rest.operation.RestOperation;

import org.junit.Before;
import org.mockito.Mock;

public class RetrieveDocumentOperationTestCase extends AsbtractOperationTestCase
{
    @Mock
    RetrieveDocumentOperation operation;

    @Before
    public void setup() throws MuleException
    {
        super.setup();
        operation = new RetrieveDocumentOperation();
        operation.setHandler(handler);
    }

    @Override
    public RestOperation getOperation()
    {
        return operation;
    }

}
