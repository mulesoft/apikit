/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.emitter;

import org.raml.emitter.RamlEmitter;
import org.mule.raml.interfaces.emitter.IRamlEmitter;
import org.mule.raml.interfaces.model.IRaml;
import org.raml.model.Raml;

public class RamlEmitterImpl implements IRamlEmitter
{
    RamlEmitter ramlEmitter;
    public RamlEmitterImpl()
    {
        ramlEmitter = new RamlEmitter();
    }

    public String dump(IRaml iRaml)
    {
        return ramlEmitter.dump((Raml) iRaml.getInstance());
    }
}
