/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.StartException;
import org.mule.api.registry.RegistrationException;
import org.mule.config.i18n.MessageFactory;
import org.mule.construct.Flow;

import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Router extends AbstractRouter
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ConsoleHandler consoleHandler;

    public void setConfig(Configuration config)
    {
        this.config = config;
    }

    public Configuration getConfig()
    {
        return (Configuration) config;
    }

    @Override
    protected void startConfiguration() throws StartException
    {
        if (config == null)
        {
            try
            {
                config = muleContext.getRegistry().lookupObject(Configuration.class);
            }
            catch (RegistrationException e)
            {
                throw new StartException(MessageFactory.createStaticMessage("APIKit configuration not Found"), this);
            }
        }
        config.loadApiDefinition(flowConstruct);
        if (getConfig().isConsoleEnabled())
        {
            consoleHandler = new ConsoleHandler(getApi().getBaseUri(), getConfig().getConsolePath());
            getConfig().addConsoleUrl(consoleHandler.getConsoleUrl());
        }
        getConfig().publishConsoleUrls(muleContext.getConfiguration().getWorkingDirectory());
    }

    @Override
    protected HttpRestRequest getHttpRestRequest(MuleEvent event)
    {
        return new HttpRestRouterRequest(event, getConfig());
    }

    @Override
    protected MuleEvent handleEvent(MuleEvent event, String path) throws MuleException
    {
        //check for console request
        if (getConfig().isConsoleEnabled() && path.startsWith(getApi().getUri() + "/" + getConfig().getConsolePath()))
        {
            return consoleHandler.process(event);
        }
        return null;
    }

    @Override
    protected Flow getFlow(Resource resource, String method)
    {
        return ((Configuration) config).getRawRestFlowMap().get(method + ":" + resource.getUri());
    }

}
