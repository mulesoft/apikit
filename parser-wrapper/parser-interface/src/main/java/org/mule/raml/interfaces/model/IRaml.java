/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model;

import org.mule.raml.interfaces.model.parameter.IParameter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface IRaml extends Serializable
{
    IResource getResource(String path);
    Map<String, String> getConsolidatedSchemas();
    Map<String, Object> getCompiledSchemas();// TODO THIS MUST BE REMOVED
    String getBaseUri();
    Map<String, IResource> getResources();
    String getVersion();
    //void setBaseUri(String baseUri);
    Map<String, IParameter> getBaseUriParameters();
    //void setCompiledSchemas(Map<String, Object> compiledSchemas);
    List<Map<String, ISecurityScheme>> getSecuritySchemes();
    List<Map<String, ITemplate>> getTraits();
    String getUri();
    List<Map<String, String>> getSchemas();
    Object getInstance();
    void cleanBaseUriParameters();
    void injectTrait(String name);
    void injectSecurityScheme(Map<String, ISecurityScheme> securityScheme);
    List<String> getAllReferences();
}
