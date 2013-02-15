
package org.mule.module.wsapi.rest.resource;

import org.mule.module.wsapi.api.WebServiceRoute;
import org.mule.module.wsapi.rest.RestRequestHandler;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.swagger.json.RestResourceSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Set;

@JsonSerialize(using = RestResourceSerializer.class)
public interface RestResource extends RestRequestHandler, WebServiceRoute
{
    boolean isActionSupported(ActionType actionType);

    Set<ActionType> getSupportedActions();

}
