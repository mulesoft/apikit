
package org.mule.webservice.api;

import org.mule.api.NamedObject;
import org.mule.api.processor.MessageRouter;

import java.util.List;

public interface WebServiceInterface extends NamedObject
{

    List<WebServiceRoute> getRoutes();

    MessageRouter getOperationRouter();

}
