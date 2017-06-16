/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.mule.module.apikit.spi.ScaffolderService;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ScaffolderServiceLoader
{
    public ScaffolderService loadService()
    {
        ServiceLoader<ScaffolderService> loader = ServiceLoader.load(ScaffolderService.class);
        Iterator<ScaffolderService> it = loader.iterator();
        if (it.hasNext())
        {
            return it.next();
        }
        return null;
    }
}
