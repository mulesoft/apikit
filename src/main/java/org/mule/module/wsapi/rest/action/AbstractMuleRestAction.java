
package org.mule.module.wsapi.rest.action;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.module.wsapi.AbstractWebServiceOperation;
import org.mule.module.wsapi.rest.RestException;
import org.mule.module.wsapi.rest.RestRequest;

public abstract class AbstractMuleRestAction extends AbstractWebServiceOperation implements MuleRestAction
{

    protected ActionType type;

    @Override
    public ActionType getType()
    {
        return type;
    }

    @Override
    public MuleEvent handle(RestRequest request) throws RestException
    {
        try
        {
            return getHandler().process(request.getMuleEvent());
        }
        catch (MuleException e)
        {
            throw new RestException();
        }
    }

}
