/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.config;

import org.mule.config.spring.parsers.delegate.ParentContextDefinitionParser;
import org.mule.module.apikit.rest.resource.collection.DeleteCollectionMemberOperation;

public class RestDeleteOperationDefinitionParser extends ParentContextDefinitionParser
{
    public RestDeleteOperationDefinitionParser()
    {
        super("member", new RestOperationDefinitionParser(DeleteCollectionMemberOperation.class));
    }

}
