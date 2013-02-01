/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.webservice.rest.protocol;

import org.mule.webservice.rest.action.ResourceOperationType;

import java.net.URI;
import java.util.Map;

public interface RestProtocolAdapter
{

    ResourceOperationType getActionType();

    URI getURI();
    
    String getAcceptedContentTypes();

    String getRequestContentType();

    Map<String, Object> getQueryParameters();


}


