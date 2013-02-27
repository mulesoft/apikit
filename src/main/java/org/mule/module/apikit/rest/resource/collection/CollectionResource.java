
package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.OperationNotAllowedException;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractRestResource;
import org.mule.module.apikit.rest.resource.RestResource;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public class CollectionResource extends AbstractRestResource
{

    protected CollectionMemberResource memberResource;

    public CollectionResource(String name, RestResource parentResource)
    {
        super(name, parentResource);
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.CREATE);

    }

    public void setMemberResource(CollectionMemberResource memberResource)
    {
        this.memberResource = memberResource;
    }

    public CollectionMemberResource getMemberResource()
    {
        return memberResource;
    }

    @Override
    public void handle(RestRequest request) throws RestException
    {
        if (request.hasMorePathElements())
        {
            memberResource.handle(request);
        }
        else
        {
            processResource(request);
        }
    }

    @Override
    protected RestOperation getAction(RestOperationType actionType, RestRequest request)
        throws OperationNotAllowedException
    {
        if (actionType == RestOperationType.CREATE)
        {
            return memberResource.getOperations().get(0);
        }
        else
        {
            return super.getAction(actionType, request);
        }
    }

    @Override
    public void appendSwaggerJson(JsonGenerator jsonGenerator) throws JsonGenerationException, IOException
    {
        super.appendSwaggerJson(jsonGenerator);
        memberResource.appendSwaggerJson(jsonGenerator);
    }

}
