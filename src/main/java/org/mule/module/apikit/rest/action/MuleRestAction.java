
package org.mule.module.apikit.rest.action;

import org.mule.module.apikit.api.WebServiceOperation;

public interface MuleRestAction extends WebServiceOperation, RestAction
{

    ActionType getType();
}
