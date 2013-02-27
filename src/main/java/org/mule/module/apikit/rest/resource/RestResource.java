
package org.mule.module.apikit.rest.resource;

import org.mule.api.NamedObject;
import org.mule.module.apikit.api.WebServiceRoute;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.RestRequestHandler;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RestResource extends RestRequestHandler, WebServiceRoute, NamedObject
{
    boolean isOperationTypeAllowed(RestOperationType actionType);

    Set<RestOperationType> getAllowedOperationTypes();

    void setOperations(List<RestOperation> operation);

    List<RestOperation> getOperations();

    List<RestOperation> getAuthorizedOperations(RestRequest request);

    Collection<RepresentationMetaData> getRepresentations();

    void appendSwaggerJson(JsonGenerator jsonGenerator) throws JsonGenerationException, IOException;

    String getPath();
}
