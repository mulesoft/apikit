
package org.mule.module.apikit.rest.resource.base;

import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractHierarchicalRestResource;

import java.util.EnumSet;
import java.util.Set;

public class BaseUriResource extends AbstractHierarchicalRestResource
{

    public BaseUriResource()
    {
        super("", null);
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS);
    }

    private RestOperation getSwaggerOperation()
    {
        if (swaggerOperation == null)
        {
            SwaggerResourceDescriptorOperation op = new SwaggerResourceDescriptorOperation();
            op.setResource(this);
            swaggerOperation = op;
        }
        return swaggerOperation;
    }

}
