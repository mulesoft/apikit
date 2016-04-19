/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces;

import org.mule.raml.interfaces.parser.visitor.IRamlValidationService;
import org.mule.raml.interfaces.emitter.IRamlEmitter;
import org.mule.raml.interfaces.parser.visitor.IRamlCloningService;
import org.mule.raml.interfaces.parser.visitor.IRamlDocumentBuilder;

public interface IRamlFactory
{
    IRamlEmitter createRamlEmitter();

    IRamlDocumentBuilder createRamlDocumentBuilder();

    IRamlValidationService createRamlValidationService(IRamlDocumentBuilder ramlDocumentBuilder);

    IRamlCloningService createRamlCloningService();

}
