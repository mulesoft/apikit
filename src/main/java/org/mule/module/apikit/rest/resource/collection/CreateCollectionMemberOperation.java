package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;


public class CreateCollectionMemberOperation extends AbstractRestOperation
{

    public CreateCollectionMemberOperation()
    {
        this.type = RestOperationType.CREATE;
    }

}
