/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.interfaces;

import org.mule.metadata.api.model.FunctionType;

import java.util.Optional;

/**
 * Represents a source of Metadata
 *
 * Related clases:
 * {@link org.mule.module.apikit.metadata.FlowMetadata}
 */
public interface MetadataSource
{
    /**
     * Generates the input and output metadata
     * @return A FunctionType that represents the input and output metadata. If
     * the source has nothing to show, it will return {@link Optional#empty()}
     */
    Optional<FunctionType> getMetadata();
}
