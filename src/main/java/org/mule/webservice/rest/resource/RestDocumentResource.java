
package org.mule.webservice.rest.resource;

import static org.mule.webservice.rest.action.ResourceOperationType.RETRIEVE;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.webservice.rest.action.ResourceOperationType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class RestDocumentResource extends AbstractRestResource
{
    public static final Set<ResourceOperationType> supportedActions = EnumSet.of(RETRIEVE);

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<ResourceOperationType> getSupportedActions()
    {
        return supportedActions;
    }
}
