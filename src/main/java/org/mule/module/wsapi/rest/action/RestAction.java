
package org.mule.module.wsapi.rest.action;

import org.mule.api.processor.MessageProcessor;
import org.mule.module.wsapi.api.WebServiceOperation;

public interface RestAction extends WebServiceOperation, MessageProcessor
{

    ActionType getType();
}
