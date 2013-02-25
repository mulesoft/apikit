
package org.mule.module.apikit.rest.resource.collection;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.UnexpectedException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;

import java.net.URI;
import java.net.URISyntaxException;

public class CreateCollectionMemberOperation extends AbstractRestOperation
{

    @Override
    public MuleEvent handle(RestRequest request) throws RestException
    {
        super.handle(request);
        try
        {
            request.getProtocolAdaptor().handleCreated(
                new URI(request.getProtocolAdaptor().getURI().toString() + "/"
                        + request.getMuleEvent().getFlowVariable("resourceId")), request);
        }
        catch (URISyntaxException e)
        {
            throw new UnexpectedException(e);
        }
        request.getMuleEvent().getMessage().setPayload("");
        return request.getMuleEvent();
    }

    public CreateCollectionMemberOperation()
    {
        this.type = RestOperationType.CREATE;
    }

}
