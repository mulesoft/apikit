
package org.mule.module.wsapi.rest.resource;

import org.mule.api.MuleEvent;
import org.mule.module.wsapi.rest.RestException;
import org.mule.module.wsapi.rest.RestRequest;
import org.mule.module.wsapi.rest.RestWebService;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.action.BaseUriRetrieveAction;
import org.mule.module.wsapi.rest.action.RestAction;

import java.util.Collections;
import java.util.Set;

public class BaseResource extends AbstractRestResource
{

    public BaseResource(RestWebService restWebService)
    {
        actions = Collections.<RestAction> singletonList(new BaseUriRetrieveAction(restWebService));
    }

    @Override
    public Set<ActionType> getSupportedActions()
    {
        return Collections.singleton(ActionType.RETRIEVE);
    }

    @Override
    protected MuleEvent processResource(RestRequest restCall) throws RestException
    {
        System.out.println("PROCESSING BASE RESOURCE");

        return super.processResource(restCall);
    }

    @Override
    public String getName()
    {
        return "";
    }

    @Override
    public String getTemplateUri()
    {
        return "";
    }

}
