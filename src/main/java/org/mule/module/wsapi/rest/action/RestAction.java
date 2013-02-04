
package org.mule.module.wsapi.rest.action;

import org.mule.module.wsapi.api.WebServiceOperation;

public interface RestAction extends WebServiceOperation
{

    ActionType getType();
}
