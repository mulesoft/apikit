
package org.mule.module.apikit.rest.resource;

import org.mule.api.lifecycle.Initialisable;

import java.util.List;

public interface HierarchicalRestResource extends RestResource, Initialisable
{
    List<RestResource> getResources();


}
