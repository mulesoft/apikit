
package org.mule.module.wsapi.rest.resource;

import org.mule.module.wsapi.rest.RestWebServiceInterface;
import org.mule.module.wsapi.rest.action.ActionType;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestBaseResource extends AbstractRestResource
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public RestBaseResource(RestWebServiceInterface restInterface)
    {
        this.restInterface = restInterface;
        setRoutes(restInterface.getRoutes());
        logger.debug("Creating REST resource hierarchy and updating routing table...");
        buildRoutingTable();
    }

    @Override
    public Set<ActionType> getSupportedActions()
    {
        return Collections.singleton(ActionType.RETRIEVE);
    }

}
