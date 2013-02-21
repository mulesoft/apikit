
package org.mule.module.apikit.rest.resource;

import org.mule.api.NamedObject;
import org.mule.module.apikit.api.WebServiceRoute;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.RestRequestHandler;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;

import java.util.List;
import java.util.Set;

public interface RestResource extends RestRequestHandler, WebServiceRoute, NamedObject
{
    boolean isActionTypeAllowed(RestOperationType actionType);

    Set<RestOperationType> getAllowedActionTypes();

    void setActions(List<RestOperation> actions);

    List<RestOperation> getActions();

    List<RestOperation> getAuthorizedActions(RestRequest request);

}
