/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest;

import org.mule.api.MessagingException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.apikit.AbstractWebService;
import org.mule.module.apikit.rest.documentation.swagger.SwaggerResourceDocumentationStrategy;
import org.mule.module.apikit.rest.resource.HierarchicalRestResource;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.module.apikit.rest.resource.base.BaseResource;
import org.mule.module.apikit.rest.swagger.SwaggerConsoleResource;

import java.util.ArrayList;
import java.util.List;

public class RestWebService extends AbstractWebService<RestWebServiceInterface>
{
    protected boolean enableSwagger;
    protected String swaggerConsolePath = "console";
    protected HierarchicalRestResource baseResource;
    protected RestDocumentationStrategy documentationStrategy = new SwaggerResourceDocumentationStrategy();

    public RestWebService(String name,
                          RestWebServiceInterface webServiceInterface,
                          boolean enableSwagger,
                          MuleContext muleContext)
    {
        super(name, webServiceInterface, muleContext);
    }

    @Override
    protected void doInitialise() throws MuleException
    {
        super.doInitialise();
        BaseResource resource = new BaseResource();
        List<RestResource> resources = new ArrayList<RestResource>();
        resources.addAll((List<RestResource>) webServiceInterface.getRoutes());
        if (enableSwagger)
        {
            resources.add(new SwaggerConsoleResource(swaggerConsolePath, resource));
        }
        resource.setResources(resources);
        resource.initialise();
        this.baseResource = resource;
    }

    @Override
    public String getConstructType()
    {
        return "REST-WEB-SERVICE";
    }

    public boolean isEnableSwagger()
    {
        return enableSwagger;
    }

    public void setEnableSwagger(boolean enableSwagger)
    {
        this.enableSwagger = enableSwagger;
    }

    public HierarchicalRestResource getBaseResource()
    {
        return baseResource;
    }

    @Override
    protected MessageProcessor getRequestRouter() throws InitialisationException
    {
        return new MessageProcessor()
        {
            @Override
            public MuleEvent process(MuleEvent event) throws MessagingException
            {
                try
                {
                    RestRequest request = new DefaultRestRequest(event, RestWebService.this);
                    baseResource.handle(request);
                    return request.getMuleEvent();
                }
                catch (RestException e)
                {
                    throw new MessagingException(event, e);
                }
            }
        };

    }

    public RestDocumentationStrategy getDocumentationStrategy()
    {
        return documentationStrategy;
    }

    public void setDocumentationStrategy(RestDocumentationStrategy documentationStrategy)
    {
        this.documentationStrategy = documentationStrategy;
    }
}
