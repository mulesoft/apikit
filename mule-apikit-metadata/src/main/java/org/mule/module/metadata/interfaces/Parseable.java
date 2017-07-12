package org.mule.module.metadata.interfaces;

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
