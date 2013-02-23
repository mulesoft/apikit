package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;


public class DeleteCollectionMemberOperation extends AbstractRestOperation
{

    public DeleteCollectionMemberOperation()
    {
        this.type = RestOperationType.DELETE;
    }

}
