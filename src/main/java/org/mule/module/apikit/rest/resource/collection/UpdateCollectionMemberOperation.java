package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;


public class UpdateCollectionMemberOperation extends AbstractRestOperation
{

    public UpdateCollectionMemberOperation()
    {
        this.type = RestOperationType.UPDATE;
    }

}
