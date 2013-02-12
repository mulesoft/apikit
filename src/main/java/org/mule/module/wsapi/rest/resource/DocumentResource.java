
package org.mule.module.wsapi.rest.resource;

import static org.mule.module.wsapi.rest.action.ActionType.EXISTS;
import static org.mule.module.wsapi.rest.action.ActionType.RETRIEVE;
import static org.mule.module.wsapi.rest.action.ActionType.UPDATE;

import org.mule.module.wsapi.rest.action.ActionType;

import java.util.EnumSet;
import java.util.Set;

public class DocumentResource extends AbstractRestResource
{

    public static final Set<ActionType> supportedActions = EnumSet.of(RETRIEVE, UPDATE, EXISTS);

    @Override
    public Set<ActionType> getSupportedActions()
    {
        return supportedActions;
    }
}
