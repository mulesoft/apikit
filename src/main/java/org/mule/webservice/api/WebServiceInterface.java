
package org.mule.webservice.api;

import org.mule.api.NamedObject;

import java.util.List;

public interface WebServiceInterface extends NamedObject
{

    List<ServiceOperation> getOperations();

    ServiceOperationRouter getServiceOperationRouter();

}
