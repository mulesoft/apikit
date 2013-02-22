
package org.mule.module.apikit.rest.resource.base;

import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractHierarchicalRestResource;

import java.util.EnumSet;
import java.util.Set;

public class BaseUriResource extends AbstractHierarchicalRestResource
{

    public BaseUriResource()
    {
        super("");
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS);
    }

}
