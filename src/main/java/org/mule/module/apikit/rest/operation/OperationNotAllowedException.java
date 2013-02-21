
package org.mule.module.apikit.rest.operation;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.resource.RestResource;

public class OperationNotAllowedException extends RestException
{

    private static final long serialVersionUID = 7820998020825499825L;

    protected RestResource resource;
    protected RestOperationType actionType;

    public OperationNotAllowedException(RestResource resource, RestOperationType actionType)
    {
        this.resource = resource;
        this.actionType = actionType;
    }

    public RestResource getResource()
    {
        return resource;
    }

}
