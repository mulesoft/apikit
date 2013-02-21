
package org.mule.module.apikit.rest.operation;

import static org.mule.module.apikit.rest.operation.RestOperationType.EXISTS;

import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.UnsupportedMediaTypeException;

public class ExistsOperation extends AbstractRestOperation
{

    public ExistsOperation()
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
