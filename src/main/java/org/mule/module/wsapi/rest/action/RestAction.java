
package org.mule.module.wsapi.rest.action;

import org.mule.module.wsapi.rest.RestRequestHandler;

public interface RestAction extends RestRequestHandler
{

    ActionType getType();
}
