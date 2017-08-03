package org.mule.module.metadata.interfaces;

import org.mule.metadata.api.model.FunctionType;

import java.util.Optional;

public interface MetadataSource
{
    Optional<FunctionType> getMetadata();
}
