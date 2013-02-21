
package org.mule.module.apikit.rest.resource;

import org.mule.module.apikit.rest.operation.RestOperationType;

import java.util.EnumSet;
import java.util.Set;

public class CollectionResource extends AbstractHierarchicalRestResource
{
    public CollectionResource(String name)
    {
        super(name);
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.CREATE);

    }

}
