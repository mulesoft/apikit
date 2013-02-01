
package org.mule.webservice.api;

import org.mule.api.NamedObject;
import org.mule.api.processor.MessageProcessor;

import java.util.List;

public interface WebServiceInterface extends NamedObject
{

    // API Definition
    
    List<? extends WebServiceRoute> getRoutes();

    
    // Runtime
    
    MessageProcessor getOperationRouter();

}
