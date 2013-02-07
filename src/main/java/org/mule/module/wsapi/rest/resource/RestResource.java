
package org.mule.module.wsapi.rest.resource;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.wsapi.api.WebServiceRoute;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.action.RestActionNotAllowedException;
import org.mule.module.wsapi.rest.protocol.RestProtocolAdapter;

import java.util.Deque;
import java.util.List;
import java.util.Set;

public interface RestResource extends WebServiceRoute
{
    String getTemplateUri();

    List<RestResource> getResources();

    MessageProcessor getAction(ActionType actionType, MuleEvent muleEvent)
        throws RestActionNotAllowedException;

    boolean isActionSupported(ActionType actionType);

    Set<ActionType> getSupportedActions();

    void buildRoutingTable();

    MuleEvent processPath(MuleEvent muleEvent, RestProtocolAdapter protocolAdapter) throws MuleException;

    List<? extends WebServiceRoute> getRoutes();
}
