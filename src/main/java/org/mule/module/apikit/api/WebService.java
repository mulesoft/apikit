
package org.mule.module.apikit.api;

import org.mule.api.construct.FlowConstruct;

public interface WebService extends FlowConstruct
{

    WebServiceInterface getInterface();
    
    String getDescription();

}
