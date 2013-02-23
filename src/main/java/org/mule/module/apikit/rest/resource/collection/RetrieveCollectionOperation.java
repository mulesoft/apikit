package org.mule.module.apikit.rest.resource.collection;

import static org.mule.module.apikit.rest.operation.RestOperationType.RETRIEVE;

import org.mule.module.apikit.rest.operation.AbstractRestOperation;


public class RetrieveCollectionOperation extends AbstractRestOperation
{

    public RetrieveCollectionOperation()
    {
        this.type = RETRIEVE;
    }

}
