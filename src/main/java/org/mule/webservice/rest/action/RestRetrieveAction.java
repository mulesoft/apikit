package org.mule.webservice.rest.action;

import static org.mule.webservice.rest.action.ResourceOperationType.RETRIEVE;

public class RestRetrieveAction extends RestAbstractAction
{

    public RestRetrieveAction()
    {
        this.type = RETRIEVE;
    }
}
