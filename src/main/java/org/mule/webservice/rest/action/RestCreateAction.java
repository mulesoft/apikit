package org.mule.webservice.rest.action;

import static org.mule.webservice.rest.action.ResourceOperationType.CREATE;

public class RestCreateAction extends RestAbstractAction
{

    public RestCreateAction()
    {
        this.type = CREATE;
    }
}
