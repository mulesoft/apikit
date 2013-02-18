
package org.mule.module.apikit.rest.action;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.module.apikit.AbstractWebServiceOperation;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;

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
