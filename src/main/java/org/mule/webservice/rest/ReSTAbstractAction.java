package org.mule.webservice.rest;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;

public abstract class ReSTAbstractAction implements ReSTAction
{

    protected String name;

    protected MessageProcessor flow;

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public MessageProcessor getFlow()
    {
        return flow;
    }

    public void setFlow(MessageProcessor flow)
    {
        this.flow = flow;
    }

    @Override
    public MuleEvent process(MuleEvent muleEvent) throws MuleException
    {
        return null;
    }
}
