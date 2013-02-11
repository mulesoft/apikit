package org.mule.module.wsapi.rest.action;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;

public abstract class AbstractRestAction implements RestAction
{

    protected String name;
    protected ActionType type;
    protected MessageProcessor flow;

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public MessageProcessor getHandler()
    {
        return flow;
    }

    public void setFlow(MessageProcessor flow)
    {
        this.flow = flow;
    }

    @Override
    public ActionType getType()
    {
        return type;
    }

    @Override
    public MuleEvent process(MuleEvent muleEvent) throws MuleException
    {
        return getHandler().process(muleEvent);
    }
}
