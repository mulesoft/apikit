
package org.mule.module.apikit;

import org.mule.module.apikit.api.WebServiceInterface;
import org.mule.module.apikit.api.WebServiceRoute;
import org.mule.module.apikit.rest.RestRequest;

import java.util.List;

public abstract class AbstractWebServiceRoute implements WebServiceInterface
{
    protected String name;
    protected String description;
    protected String accessExpression;

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

    @Override
    public String getAccessExpression()
    {
        return accessExpression;
    }

    public void setAccessExpression(String accessExpression)
    {
        this.accessExpression = accessExpression;
    }
}
