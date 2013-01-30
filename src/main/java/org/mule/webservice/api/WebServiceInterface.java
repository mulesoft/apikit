
package org.mule.webservice.api;

import org.mule.api.NamedObject;

import java.util.List;

public interface WebServiceInterface extends NamedObject
{

    List<WebServiceRoute> getRoutes();

    WebServiceOperationRouter getOperationRouter();

}
