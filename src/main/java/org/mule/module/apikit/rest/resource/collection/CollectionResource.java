
package org.mule.module.apikit.rest.resource.collection;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.OperationNotAllowedException;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractRestResource;

import java.util.EnumSet;
import java.util.Set;

public class CollectionResource extends AbstractRestResource
{

    protected CollectionMemberResource memberResource;

    public CollectionResource(String name)
    {
        super(name);
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.CREATE);

    }

    public void setMemberResource(CollectionMemberResource memberResource)
    {
        this.memberResource = memberResource;
        memberResource.setCollectionResource(this);
    }

    public CollectionMemberResource getMemberResource()
    {
        return memberResource;
    }

    @Override
    public MuleEvent handle(RestRequest request) throws RestException
    {
        if (request.hasMorePathElements())
        {
            return memberResource.handle(request);
        }
        else
        {
            return processResource(request);
        }
    }

    @Override
    protected RestOperation getAction(RestOperationType actionType, MuleEvent muleEvent)
        throws OperationNotAllowedException
    {
        if (actionType == RestOperationType.CREATE)
        {
            return memberResource.getOperations().get(0);
        }
        else
        {
            return super.getAction(actionType, muleEvent);
        }
    }

    String getMemberIdFlowVarName()
    {
        return getName() + "MemberId";
    }
}
