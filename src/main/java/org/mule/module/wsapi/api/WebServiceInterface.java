
package org.mule.module.wsapi.api;

import org.mule.api.NamedObject;

import java.util.List;

public interface WebServiceInterface extends NamedObject
{

    // API Definition

    String getDescription();

    List<? extends WebServiceRoute> getRoutes();

}
