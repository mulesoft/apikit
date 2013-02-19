
package org.mule.module.apikit.rest.action;

import org.mule.module.apikit.api.WebServiceOperation;
import org.mule.module.apikit.rest.RestRequestHandler;

public interface RestAction extends RestRequestHandler, WebServiceOperation
{

    ActionType getType();
}
