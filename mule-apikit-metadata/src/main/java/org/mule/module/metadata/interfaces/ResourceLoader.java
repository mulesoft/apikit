package org.mule.module.metadata.interfaces;

import org.mule.raml.interfaces.model.IRaml;

public interface ResourceLoader
{
    IRaml getRamlApi(String uri);
}
