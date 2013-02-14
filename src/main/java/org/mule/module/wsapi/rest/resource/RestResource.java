
package org.mule.module.wsapi.rest.resource;

import org.mule.module.wsapi.api.WebServiceRoute;
import org.mule.module.wsapi.rest.RestRequestHandler;
import org.mule.module.wsapi.rest.action.ActionType;

import java.util.Set;

public interface RestResource extends RestRequestHandler, WebServiceRoute
{
    boolean isActionSupported(ActionType actionType);

    Set<ActionType> getSupportedActions();

}
