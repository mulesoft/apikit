/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.parser.visitor;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.SerializationUtils;
import org.mule.raml.implv1.model.RamlImplV1;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.visitor.IRamlCloningService;
import org.raml.model.Raml;

public class RamlCloningServiceImpl implements IRamlCloningService
{

    public IRaml deepCloneRaml(IRaml source)
    {
        Raml sourceInstance = (Raml)source.getInstance();
        Raml targetInstance = (Raml) SerializationUtils.deserialize(SerializationUtils.serialize(sourceInstance));
        targetInstance.setCompiledSchemas(sourceInstance.getCompiledSchemas());
        return new RamlImplV1(targetInstance);
    }

    public IRaml shallowCloneRaml(IRaml source)
    {
        Raml sourceInstance = (Raml) source.getInstance();
        try
        {
             return new RamlImplV1((Raml) BeanUtils.cloneBean(sourceInstance));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
