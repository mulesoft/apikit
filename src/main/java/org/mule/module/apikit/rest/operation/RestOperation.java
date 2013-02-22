
package org.mule.module.apikit.rest.operation;

import org.mule.module.apikit.api.WebServiceOperation;
import org.mule.module.apikit.rest.RestRequestHandler;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;

import java.util.Collection;

public interface RestOperation extends RestRequestHandler, WebServiceOperation
{

    RestOperationType getType();

    Collection<RepresentationMetaData> getRepresentations();
    
}
