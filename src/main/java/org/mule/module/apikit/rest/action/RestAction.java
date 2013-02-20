
package org.mule.module.apikit.rest.action;

import org.mule.module.apikit.api.WebServiceOperation;
import org.mule.module.apikit.rest.RestRequestHandler;
import org.mule.module.apikit.rest.representation.Representation;

import java.util.Collection;

public interface RestAction extends RestRequestHandler, WebServiceOperation
{

    ActionType getType();

    Collection<Representation> getRepresentations();

}
