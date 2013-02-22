package org.mule.module.apikit.rest.resource.document;

import static org.mule.module.apikit.rest.operation.RestOperationType.UPDATE;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;


public class UpdateDocumentOperation extends AbstractRestOperation
{

    public UpdateDocumentOperation()
    {
        this.type = UPDATE;
    }
}
