
package org.mule.module.apikit.rest.resource.collection;

import static org.mule.module.apikit.rest.swagger.SwaggerConstants.DESCRIPTION_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.OPERATIONS_FIELD_NAME;
import static org.mule.module.apikit.rest.swagger.SwaggerConstants.PATH_FIELD_NAME;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.param.PathParameter;
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
        parameters.add(new PathParameter(parentResource.getName() + "MemberId"));
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

    @Override
    public void appendSwaggerJson(JsonGenerator jsonGenerator) throws JsonGenerationException, IOException
    {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName(PATH_FIELD_NAME);
        jsonGenerator.writeString(getPath());
        jsonGenerator.writeFieldName(DESCRIPTION_FIELD_NAME);
        jsonGenerator.writeString(getDescription().trim());
        jsonGenerator.writeFieldName(OPERATIONS_FIELD_NAME);
        jsonGenerator.writeStartArray();

        for (RestOperation operation : getOperations())
        {
            if (!operation.getType().equals(RestOperationType.CREATE))
            {
                operation.appendSwaggerDescriptor(jsonGenerator);
            }
        }

        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
        for (RestResource resource : getResources())
        {
            resource.appendSwaggerJson(jsonGenerator);
        }
    }

    @Override
    public String getDescription()
    {
        if (super.getDescription() != null)
        {
            return super.getDescription();
        }
        else
        {
            return getName() + " Collection Member";
        }
    }

}
