/*
 * $Id: FlowDefinitionParser.java 22557 2011-07-25 22:48:27Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.config;

import org.mule.module.apikit.rest.resource.document.RetrieveDocumentOperation;

public class RestCreateOperationDefinitionParser extends AbstractRestOperationDefinitionParser
{
    public RestCreateOperationDefinitionParser()
    {
        super(RetrieveDocumentOperation.class);
    }

}
