
package org.mule.webservice.rest;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.webservice.api.Operation;

import java.util.List;

public abstract class AbstractReSTResource implements Operation
{

    protected List<ReSTAction> actions;
    protected ReSTProtocolAdaptor protocolAdaptor;
    
    @Override
    public final MuleEvent process(MuleEvent event) throws MuleException
    {
        return null;
    }
    
    @Override
    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public String getRoles()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}

