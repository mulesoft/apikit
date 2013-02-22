package org.mule.module.apikit.rest.resource.document;

import static org.mule.module.apikit.rest.operation.RestOperationType.RETRIEVE;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;


public class RetrieveDocumentOperation extends AbstractRestOperation
{

    public RetrieveDocumentOperation()
    {
        this.type = RETRIEVE;
    }

}
