
package org.mule.module.apikit.rest.operation;

import org.mule.module.apikit.api.WebServiceOperation;
import org.mule.module.apikit.rest.RestRequestHandler;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;

import java.io.IOException;
import java.util.Collection;

import org.codehaus.jackson.JsonGenerator;

public interface RestOperation extends RestRequestHandler, WebServiceOperation
{

    RestOperationType getType();

    Collection<RepresentationMetaData> getRepresentations();

    void appendSwaggerDescriptor(JsonGenerator jsonGenerator) throws IOException;
}
