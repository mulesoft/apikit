
package org.mule.module.apikit.rest.resource.collection;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractHierarchicalRestResource;
import org.mule.module.apikit.rest.resource.ResourceNotFoundException;
import org.mule.module.apikit.rest.resource.RestResource;

import java.util.EnumSet;
import java.util.Set;

public class CollectionMemberResource extends AbstractHierarchicalRestResource
{
    public CollectionMemberResource()
    {
        super(null);
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.UPDATE,
            RestOperationType.DELETE);
    }

    @Override
    public MuleEvent handle(RestRequest restRequest) throws RestException
    {
        try
        {
            restRequest.getMuleEvent().setFlowVariable("resourceId", restRequest.getNextPathElement());
            if (restRequest.hasMorePathElements())
            {
                String path = restRequest.getNextPathElement();
                RestResource resource = routingTable.get(path);
                if (resource != null)
                {
                    resource.handle(restRequest);
                }
                else
                {
                    throw new ResourceNotFoundException(path);
                }
            }
            else
            {
                processResource(restRequest);
            }
        }
        catch (RestException re)
        {
            restRequest.getProtocolAdaptor().handleException(re, restRequest);

        }
        return restRequest.getMuleEvent();
    }

}
