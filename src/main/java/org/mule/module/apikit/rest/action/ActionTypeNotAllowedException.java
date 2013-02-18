
package org.mule.module.apikit.rest.action;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.resource.RestResource;

public class ActionTypeNotAllowedException extends RestException
{

    private static final long serialVersionUID = 7820998020825499825L;

    protected RestResource resource;
    protected ActionType actionType;

    public ActionTypeNotAllowedException(RestResource resource, ActionType actionType)
    {
        this.resource = resource;
        this.actionType = actionType;
    }

    public RestResource getResource()
    {
        return resource;
    }

}
