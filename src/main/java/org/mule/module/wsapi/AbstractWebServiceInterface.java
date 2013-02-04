
package org.mule.module.wsapi;

import org.mule.module.wsapi.api.WebServiceInterface;
import org.mule.module.wsapi.api.WebServiceRoute;

import java.util.List;

public abstract class AbstractWebServiceInterface implements WebServiceInterface
{
    protected String name;

    protected List<WebServiceRoute> routes;

    public AbstractWebServiceInterface(String name)
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

}
