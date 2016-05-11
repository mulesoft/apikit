/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parser;

import org.mule.module.apikit.AbstractConfiguration;
import org.mule.module.apikit.injector.RamlUpdater;
import org.mule.raml.interfaces.model.IRaml;

public interface ParserWrapper
{
    void validate();

    IRaml build();

    String dump(String ramlContent, IRaml api, String oldSchemeHostPort, String newSchemeHostPort);

    String dump(IRaml api, String newBaseUri);

    RamlUpdater getRamlUpdater(IRaml api, AbstractConfiguration configuration);

    void updateBaseUri(IRaml api, String baseUri);
}
