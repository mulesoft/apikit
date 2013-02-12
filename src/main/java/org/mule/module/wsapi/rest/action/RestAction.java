
package org.mule.module.wsapi.rest.action;

import org.mule.module.wsapi.api.WebServiceOperation;
import org.mule.module.wsapi.rest.RestRequestHandler;

public interface RestAction extends WebServiceOperation, RestRequestHandler
{

    ActionType getType();
}
