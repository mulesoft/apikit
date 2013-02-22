
package org.mule.module.apikit.rest.resource.document;

import static org.mule.module.apikit.rest.operation.RestOperationType.EXISTS;

import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.UnsupportedMediaTypeException;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;

public class ExistsDocumentOperation extends AbstractRestOperation
{

    public ExistsDocumentOperation()
    {
        this.type = EXISTS;
    }

    @Override
    protected void validateAcceptableResponeMediaType(RestRequest request)
        throws MediaTypeNotAcceptableException
    {
        // MediaTypes should be ignored for this action.
    }

    @Override
    protected void validateSupportedRequestMediaType(RestRequest request)
        throws UnsupportedMediaTypeException
    {
        // MediaTypes should be ignored for this action.
    }
}
