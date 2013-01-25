
package org.mule.webservice.rest;

import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.webservice.api.ServiceOperationRouter;

import java.util.List;

public abstract class AbstractReSTResource implements ServiceOperationRouter
{

    protected String name;

    protected List<ReSTAction> actions;
    protected ReSTProtocolAdaptor protocolAdaptor;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void addRoute(MessageProcessor processor) throws MuleException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeRoute(MessageProcessor processor) throws MuleException
    {
        // TODO Auto-generated method stub

    }

}
