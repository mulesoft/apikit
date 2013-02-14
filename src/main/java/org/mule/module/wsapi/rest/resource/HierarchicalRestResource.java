
package org.mule.module.wsapi.rest.resource;

import java.util.List;

public interface HierarchicalRestResource extends RestResource
{
    List<RestResource> getResources();


}
