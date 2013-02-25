
package org.mule.module.apikit.rest.resource.collection;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.document.UpdateDocumentOperation;

public class UpdateCollectionMemberOperation extends UpdateDocumentOperation
{

    public UpdateCollectionMemberOperation()
    {
        this.type = RestOperationType.UPDATE;
    }

    @Override
    public MuleEvent handle(RestRequest request) throws RestException
    {
        MuleEvent event = super.handle(request);
        request.getMuleEvent().getMessage().setPayload("");
        return event;
    }

}
