package org.mule.module.wsapi.rest.action;

import static org.mule.module.wsapi.rest.action.ActionType.RETRIEVE;


public class RestRetrieveAction extends AbstractMuleRestAction
{

    public RestRetrieveAction()
    {
        this.type = RETRIEVE;
    }
}
