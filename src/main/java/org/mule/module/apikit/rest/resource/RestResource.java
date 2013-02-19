
package org.mule.module.apikit.rest.resource;

import org.mule.api.NamedObject;
import org.mule.module.apikit.api.WebServiceRoute;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.RestRequestHandler;
import org.mule.module.apikit.rest.action.ActionType;
import org.mule.module.apikit.rest.action.RestAction;
import org.mule.module.apikit.rest.swagger.json.RestResourceSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.Set;

@JsonSerialize(using = RestResourceSerializer.class)
public interface RestResource extends RestRequestHandler, WebServiceRoute, NamedObject
{
    boolean isActionTypeAllowed(ActionType actionType);

    Set<ActionType> getAllowedActionTypes();

    void setActions(List<RestAction> actions);

    List<RestAction> getActions();

    List<RestAction> getAuthorizedActions(RestRequest request);

}
