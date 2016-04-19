/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces;

import org.mule.raml.interfaces.emitter.IRamlEmitter;
import org.mule.raml.interfaces.parser.visitor.IRamlCloningService;
import org.mule.raml.interfaces.parser.visitor.IRamlDocumentBuilder;
import org.mule.raml.interfaces.parser.visitor.IRamlValidationService;

import java.util.Iterator;
import java.util.ServiceLoader;

public class RamlFactoryHelper
{
    private static IRamlFactory iRamlFactory;

    private static IRamlFactory loadFactory()
    {
        if (iRamlFactory != null)
        {
            return iRamlFactory;
        }
        ServiceLoader<IRamlFactory> loader = ServiceLoader.load(IRamlFactory.class);
        Iterator<IRamlFactory> it = loader.iterator();
        if (it.hasNext())
        {
            iRamlFactory = it.next();
            return iRamlFactory;
        }
        throw new RuntimeException("RamlFactoryLoader couldn't find any RamlFactory");
    }

    public static IRamlEmitter createRamlEmitter()
    {
        return loadFactory().createRamlEmitter();
    }

    public static IRamlDocumentBuilder createRamlDocumentBuilder()
    {
        return loadFactory().createRamlDocumentBuilder();
    }

    public static IRamlValidationService createRamlValidationService(IRamlDocumentBuilder ramlDocumentBuilder)
    {
        return loadFactory().createRamlValidationService(ramlDocumentBuilder);
    }

    public static IRamlCloningService createRamlCloningService()
    {
        return loadFactory().createRamlCloningService();
    }

}