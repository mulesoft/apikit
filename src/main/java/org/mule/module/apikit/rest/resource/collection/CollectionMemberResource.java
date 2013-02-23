
package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractHierarchicalRestResource;

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
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.UPDATE);
    }

}
