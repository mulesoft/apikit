
package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.transport.NullPayload;

public class DeleteCollectionMemberOperation extends AbstractRestOperation
{

    public DeleteCollectionMemberOperation()
    {
        this.type = RestOperationType.DELETE;
    }

    @Override
    public void handle(RestRequest request) throws RestException
    {
        super.handle(request);
        request.getProtocolAdaptor().handleNoContent(request);
        request.getMuleEvent().getMessage().setPayload(NullPayload.getInstance());
    }
}
