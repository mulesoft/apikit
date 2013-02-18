
package org.mule.module.apikit.rest.resource;

import java.util.List;

public interface HierarchicalRestResource extends RestResource
{
    List<RestResource> getResources();


}
