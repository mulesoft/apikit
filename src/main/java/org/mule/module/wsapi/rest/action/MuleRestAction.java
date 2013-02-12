
package org.mule.module.wsapi.rest.action;

import org.mule.module.wsapi.api.WebServiceOperation;

public interface MuleRestAction extends WebServiceOperation, RestAction
{

    ActionType getType();
}
