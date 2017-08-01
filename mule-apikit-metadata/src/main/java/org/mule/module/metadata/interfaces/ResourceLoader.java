package org.mule.module.metadata.interfaces;

import java.io.File;

/**
 * Represents a way of getting resources from the application
 */
public interface ResourceLoader
{
    /**
     * Gets the root RAML File
     *
     * @param relativePath Location of the root RAML file relative to the /mule/resources/api folder
     * @return The File containing the RAML and all its includes
     */
    File getRamlResource(String relativePath);
}
