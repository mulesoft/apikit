
package org.mule.module.apikit.rest.resource.document;

import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractHierarchicalRestResource;
import org.mule.module.apikit.rest.resource.RestResource;

import java.util.EnumSet;
import java.util.Set;

public class DocumentResource extends AbstractHierarchicalRestResource
{
    public DocumentResource(String name, RestResource parentResource)
    {
        super(name, parentResource);
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.UPDATE);
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
            return getName() + " Document";
        }
    }

}
