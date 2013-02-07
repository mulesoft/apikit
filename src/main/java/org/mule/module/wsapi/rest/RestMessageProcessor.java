package org.mule.module.wsapi.rest;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.protocol.RestProtocolAdapter;
import org.mule.module.wsapi.rest.protocol.RestProtocolAdapterFactory;
import org.mule.module.wsapi.rest.resource.AbstractRestResource;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestMessageProcessor extends AbstractRestResource
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public RestMessageProcessor(RestWebServiceInterface restInterface)
    {
        setRoutes(restInterface.getRoutes());
        logger.debug("Creating REST resource hierarchy and updating routing table...");
        buildRoutingTable();
    }

    @Override
    public MuleEvent process(MuleEvent muleEvent) throws MuleException
    {
        RestProtocolAdapter protocolAdapter = getProtocolAdapter(muleEvent);
        return this.processPath(muleEvent, protocolAdapter);
    }

    protected RestProtocolAdapter getProtocolAdapter(MuleEvent muleEvent)
    {
        return RestProtocolAdapterFactory.getInstance().getAdapterForEvent(muleEvent);
    }

    @Override
    public Set<ActionType> getSupportedActions()
    {
        //none for the moment
        return new HashSet<ActionType>();
    }
}
