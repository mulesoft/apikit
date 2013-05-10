
package org.mule.module.apikit.rest.swagger;

import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractRestResource;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.module.apikit.rest.resource.StaticResourceCollection;

import java.util.EnumSet;
import java.util.Set;

public class SwaggerConsoleResource extends AbstractRestResource
{

    protected StaticResourceCollection staticResourceCollection;;

    public SwaggerConsoleResource(String name, RestResource parentResource)
    {
        super(name, parentResource);
        staticResourceCollection = new StaticResourceCollection(getName(), parentResource,
            "/org/mule/module/apikit/rest/swagger");
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE);
    }

}
