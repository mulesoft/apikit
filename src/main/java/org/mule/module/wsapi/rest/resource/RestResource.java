
package org.mule.module.wsapi.rest.resource;

import org.mule.api.MuleEvent;
import org.mule.module.wsapi.api.WebServiceRoute;
import org.mule.module.wsapi.rest.RestRequestHandler;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.action.RestAction;
import org.mule.module.wsapi.rest.action.RestActionNotAllowedException;

import java.util.List;
import java.util.Set;

public interface RestResource extends RestRequestHandler, WebServiceRoute
{
    String getTemplateUri();

    List<RestResource> getResources();

    RestAction getAction(ActionType actionType, MuleEvent muleEvent)
        throws RestActionNotAllowedException;

    boolean isActionSupported(ActionType actionType);

    Set<ActionType> getSupportedActions();

    void buildRoutingTable();
}
