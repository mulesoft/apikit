
package org.mule.module.apikit.rest.resource.document;

import static org.mule.module.apikit.rest.operation.RestOperationType.UPDATE;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.transport.NullPayload;

public class UpdateDocumentOperation extends AbstractRestOperation
{

    public UpdateDocumentOperation()
    {
        this.type = UPDATE;
    }

    @Override
    public void handle(RestRequest request) throws RestException
    {
        super.handle(request);
        if (request.getMuleEvent() != null && request.getMuleEvent().getMessage() != null)
        {
            request.getMuleEvent().getMessage().setPayload(NullPayload.getInstance());
        }
    }
}
