/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.config;

import org.mule.config.spring.parsers.delegate.ParentContextDefinitionParser;
import org.mule.module.apikit.rest.resource.collection.RetrieveCollectionMemberOperation;
import org.mule.module.apikit.rest.resource.collection.RetrieveCollectionOperation;
import org.mule.module.apikit.rest.resource.document.RetrieveDocumentOperation;

public class RestRetrieveOperationDefinitionParser extends ParentContextDefinitionParser
{
    public RestRetrieveOperationDefinitionParser()
    {
        super("document-resource", new RestOperationDefinitionParser(RetrieveDocumentOperation.class));
        and("collection-resource", new RestOperationDefinitionParser(RetrieveCollectionOperation.class));
        and("member", new RestOperationDefinitionParser(RetrieveCollectionMemberOperation.class));

    }

}
