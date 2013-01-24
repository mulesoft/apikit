
package org.mule.webservice.api;

import org.mule.api.NamedObject;
import org.mule.api.processor.MessageProcessor;

import java.util.List;

public interface WebServiceInterface<T extends Operation> extends NamedObject, MessageProcessor
{

    List<T> getOperations();
    
    // Strategy to choose operation

}
