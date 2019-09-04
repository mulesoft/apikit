/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;

public class HttpRestProxyRequest extends HttpRestRequest
{

    public HttpRestProxyRequest(MuleEvent event, AbstractConfiguration config)
    {
        super(event, config);
    }

    @Override
    protected void handleUnsupportedMediaType() throws UnsupportedMediaTypeException
    {
        if (config.isDisableValidations())
        {
            return;
        }
        super.handleUnsupportedMediaType();
    }

    @Override
    protected boolean throwNotAcceptable() {
        if (config.isDisableValidations()) {
            return false;
        }
        return super.throwNotAcceptable();
    }
}
