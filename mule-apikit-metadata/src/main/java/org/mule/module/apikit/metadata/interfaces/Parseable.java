/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.interfaces;

import org.mule.raml.interfaces.model.IRaml;

import java.io.File;

/**
 * Interface that wraps a parser implementation
 **/
public interface Parseable
{
    /**
     * Parses and builds the model for a RAML API
     *
     * @param ramlFile The API
     * @param ramlContent
     * @return The RAML Model
     */
    IRaml build(File ramlFile, String ramlContent);
}
