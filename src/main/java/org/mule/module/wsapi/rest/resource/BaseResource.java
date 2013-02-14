
package org.mule.module.wsapi.rest.resource;

import org.mule.module.wsapi.rest.RestWebService;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.action.BaseResourceRetrieveAction;
import org.mule.module.wsapi.rest.action.RestAction;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class BaseResource extends AbstractHierarchicalRestResource
{

    public BaseResource(RestWebService restWebService)
    {
        super("");
        actions = Collections.<RestAction> singletonList(new BaseResourceRetrieveAction(restWebService));
    }

    @Override
    public Set<ActionType> getSupportedActions()
    {
        return EnumSet.of(ActionType.RETRIEVE);
    }

}
