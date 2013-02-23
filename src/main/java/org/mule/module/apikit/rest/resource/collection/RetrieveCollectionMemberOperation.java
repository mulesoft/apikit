package org.mule.module.apikit.rest.resource.collection;

import static org.mule.module.apikit.rest.operation.RestOperationType.RETRIEVE;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;


public class RetrieveCollectionMemberOperation extends AbstractRestOperation
{

    public RetrieveCollectionMemberOperation()
    {
        this.type = RETRIEVE;
    }

}
