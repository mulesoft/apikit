
package org.mule.module.apikit.rest.action;

import org.mule.module.apikit.rest.RestRequestHandler;

public interface RestAction extends RestRequestHandler
{

    ActionType getType();
}
