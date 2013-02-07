
package org.mule.module.wsapi.rest.resource;

import static org.mule.module.wsapi.rest.action.ActionType.RETRIEVE;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.module.wsapi.api.WebServiceRoute;
import org.mule.module.wsapi.rest.action.ActionType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class RestDocumentResource extends AbstractRestResource
{
    public static final Set<ActionType> supportedActions = EnumSet.of(RETRIEVE);

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<ActionType> getSupportedActions()
    {
        return supportedActions;
    }
}
