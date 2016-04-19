/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1;

import org.mule.raml.implv1.emitter.RamlEmitterImpl;
import org.mule.raml.implv1.parser.visitor.RamlCloningServiceImpl;
import org.mule.raml.implv1.parser.visitor.RamlDocumentBuilderImpl;
import org.mule.raml.implv1.parser.visitor.RamlValidationServiceImpl;
import org.mule.raml.interfaces.IRamlFactory;
import org.mule.raml.interfaces.emitter.IRamlEmitter;
import org.mule.raml.interfaces.parser.visitor.IRamlCloningService;
import org.mule.raml.interfaces.parser.visitor.IRamlDocumentBuilder;
import org.mule.raml.interfaces.parser.visitor.IRamlValidationService;

public class RamlFactory implements IRamlFactory
{
    public IRamlEmitter createRamlEmitter()
    {
        return new RamlEmitterImpl();
    }

    public IRamlDocumentBuilder createRamlDocumentBuilder()
    {
        return new RamlDocumentBuilderImpl();
    }

    public IRamlValidationService createRamlValidationService(IRamlDocumentBuilder ramlDocumentBuilder)
    {
        return new RamlValidationServiceImpl(ramlDocumentBuilder);
    }

    public IRamlCloningService createRamlCloningService()
    {
        return new RamlCloningServiceImpl();
    }
}
