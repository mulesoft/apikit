/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit;

import org.mule.module.apikit.api.WebServiceInterface;
import org.mule.module.apikit.api.WebServiceRoute;

import java.util.List;

public abstract class AbstractWebServiceRoute implements WebServiceInterface
{
    protected String name;
    protected String description;
    protected List<WebServiceRoute> routes;

    public AbstractWebServiceRoute(String name)
    {
        this.name = name;
    }

    @Override
    public List<? extends WebServiceRoute> getRoutes()
    {
        return routes;
    }

    public void setRoutes(List<WebServiceRoute> routes)
    {
        this.routes = routes;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

}
