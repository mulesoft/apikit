/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.parser.visitor;

import org.mule.raml.interfaces.model.IRaml;

public interface IRamlDocumentBuilder
{

    IRaml build(String content, String resourceLocation);

    IRaml build(String resourceLocation);

    IRamlDocumentBuilder addPathLookupFirst(String path);

    IRamlDocumentBuilder addClassPathLookup(ClassLoader customClassLoader);

    Object getInstance();
}
