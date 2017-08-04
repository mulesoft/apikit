/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.metadata.raml;

import org.mule.module.metadata.interfaces.Parseable;
import org.mule.raml.implv1.parser.visitor.RamlDocumentBuilderImpl;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.visitor.IRamlDocumentBuilder;

import java.io.File;

public class RamlV1Parser implements Parseable
{
    @Override
    public IRaml build(File ramlFile, String ramlContent)
    {
        IRamlDocumentBuilder ramlDocumentBuilder = new RamlDocumentBuilderImpl();
        ramlDocumentBuilder.addPathLookupFirst(ramlFile.getParentFile().getPath());
        return ramlDocumentBuilder.build(ramlContent, ramlFile.getName());
    }
}
