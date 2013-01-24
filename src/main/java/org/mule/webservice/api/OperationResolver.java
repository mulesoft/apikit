package org.mule.webservice.api;

import org.mule.api.MuleEvent;

public interface OperationResolver<T extends Operation> {

	T resolveOperation(MuleEvent event);
}
