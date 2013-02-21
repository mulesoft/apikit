package org.mule.module.apikit.rest.operation;

import static org.mule.module.apikit.rest.operation.RestOperationType.RETRIEVE;

import org.mule.module.apikit.rest.representation.RepresentationFactory;


public class RetrieveOperation extends AbstractRestOperation
{

    public RetrieveOperation()
    {
        this.type = RETRIEVE;
    }

}
