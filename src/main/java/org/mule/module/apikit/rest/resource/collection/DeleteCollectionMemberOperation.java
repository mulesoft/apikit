
package org.mule.module.apikit.rest.resource.collection;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;

public class DeleteCollectionMemberOperation extends AbstractRestOperation
{

    public DeleteCollectionMemberOperation()
    {
        this.type = RestOperationType.DELETE;
    }

    @Override
    public MuleEvent handle(RestRequest request) throws RestException
    {
        MuleEvent event = super.handle(request);
        request.getMuleEvent().getMessage().setPayload("");
        return event;
    }
}
