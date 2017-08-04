/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
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
