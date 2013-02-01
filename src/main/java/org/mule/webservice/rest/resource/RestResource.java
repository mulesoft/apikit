
package org.mule.webservice.rest.resource;

import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;
import org.mule.webservice.api.WebServiceRoute;
import org.mule.webservice.rest.action.ResourceOperationType;
import org.mule.webservice.rest.action.RestActionNotAllowedException;

import java.util.List;
import java.util.Set;

public interface RestResource extends WebServiceRoute
{
    String getTemplateUri();

    List<RestResource> getResources();

    MessageProcessor getAction(ResourceOperationType actionType, MuleEvent muleEvent)
            throws RestActionNotAllowedException;

    boolean isActionSupported(ResourceOperationType actionType);

    Set<ResourceOperationType> getSupportedActions();
}
