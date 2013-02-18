
package org.mule.module.apikit.rest.resource;

import org.mule.module.apikit.rest.RestException;

public class ResourceNotFoundException extends RestException
{

    private static final long serialVersionUID = -2274813685894863042L;
    protected RestResource parentResource;
    protected String resourcePath;

    public ResourceNotFoundException(String path)
    {
        this(path, null);
    }

    public ResourceNotFoundException(String path, RestResource restResource)
    {
        this.parentResource = restResource;
        this.resourcePath = path;
    }
}
