
package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractHierarchicalRestResource;
import org.mule.module.apikit.rest.resource.RestResource;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public class CollectionMemberResource extends AbstractHierarchicalRestResource
{

    public CollectionMemberResource(RestResource parentResource)
    {
        super(parentResource.getName() + "Member", parentResource);
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.UPDATE,
            RestOperationType.DELETE);
    }

    @Override
    public void handle(RestRequest restRequest) throws RestException
    {
        restRequest.getMuleEvent().setFlowVariable(getName() + "Id", restRequest.getNextPathElement());
        super.handle(restRequest);
    }

    @Override
    public String getPath()
    {
        return parentResource.getPath() + "/{" + getName() + "Id}";
    }
    
}
