
package org.mule.module.wsapi.rest.resource;

import static org.mule.module.wsapi.rest.action.ActionType.EXISTS;
import static org.mule.module.wsapi.rest.action.ActionType.RETRIEVE;
import static org.mule.module.wsapi.rest.action.ActionType.UPDATE;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.module.wsapi.rest.action.ActionType;

import java.util.EnumSet;
import java.util.Set;

public class RestDocumentResource extends AbstractRestResource
{
    public static final Set<ActionType> supportedActions = EnumSet.of(RETRIEVE, UPDATE, EXISTS);

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
