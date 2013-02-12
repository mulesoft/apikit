
package org.mule.module.wsapi.rest.action;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.wsapi.rest.RestRequest;

public abstract class AbstractMuleRestAction implements MuleRestAction
{

    protected String name;
    protected ActionType type;
    protected MessageProcessor flow;

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
    public MuleEvent handle(RestRequest request) throws MuleException
    {
        return getHandler().process(request.getMuleEvent());
    }
}
