
package org.mule.webservice.rest.action;

import org.mule.webservice.api.WebServiceOperation;

public interface RestAction extends WebServiceOperation
{

    ResourceOperationType getType();
}
