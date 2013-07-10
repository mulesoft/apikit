/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.resource;

import org.mule.module.apikit.rest.RestDocumentationStrategy;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;

public class OptionsOperation extends AbstractRestOperation
{

    public OptionsOperation(RestResource resource)
    {
        super();
        this.resource = resource;
        this.type = RestOperationType.OPTIONS;
    }

    @Override
    public void handle(RestRequest restRequest) throws RestException
    {

        RestDocumentationStrategy documentationStrategy = restRequest.getService().getDocumentationStrategy();
        if (documentationStrategy != null)
        {
            restRequest.getMuleEvent()
                .getMessage()
                .setPayload(documentationStrategy.documentResource(resource, restRequest));
            restRequest.getMuleEvent()
                .getMessage()
                .setOutboundProperty("content-type",
                    documentationStrategy.getDocumentationMediaType().toString());
        }

    }
}
