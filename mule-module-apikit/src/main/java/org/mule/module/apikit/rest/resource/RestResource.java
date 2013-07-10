/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.resource;

import org.mule.api.NamedObject;
import org.mule.module.apikit.api.WebServiceRoute;
import org.mule.module.apikit.rest.RestRequestHandler;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.param.RestParameter;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RestResource extends RestRequestHandler, WebServiceRoute, NamedObject
{
    boolean isOperationTypeAllowed(RestOperationType actionType);

    Set<RestOperationType> getAllowedOperationTypes();

    void setOperations(List<RestOperation> operation);

    List<RestOperation> getOperations();

    Collection<RepresentationMetaData> getRepresentations();

    String getPath();

    List<RestParameter> getParameters();
}
