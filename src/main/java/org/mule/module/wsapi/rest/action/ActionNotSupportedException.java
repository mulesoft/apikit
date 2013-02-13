
package org.mule.module.wsapi.rest.action;

import org.mule.module.wsapi.rest.RestException;
import org.mule.module.wsapi.rest.resource.RestResource;

public class ActionNotSupportedException extends RestException
{

    private static final long serialVersionUID = 7820998020825499825L;

    protected RestResource resource;
    protected ActionType actionType;

    public ActionNotSupportedException(RestResource resource, ActionType actionType)
    {
        this.resource = resource;
        this.actionType = actionType;
    }

}
