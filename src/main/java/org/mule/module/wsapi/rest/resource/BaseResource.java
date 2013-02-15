
package org.mule.module.wsapi.rest.resource;

import org.mule.module.wsapi.rest.action.ActionType;

import java.util.EnumSet;
import java.util.Set;

public class BaseResource extends AbstractHierarchicalRestResource
{

    public BaseResource()
    {
        super("");
    }

    @Override
    protected Set<ActionType> getSupportedActionTypes()
    {
        return EnumSet.of(ActionType.RETRIEVE);
    }

}
