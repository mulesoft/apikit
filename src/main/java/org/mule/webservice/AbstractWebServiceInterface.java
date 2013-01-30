
package org.mule.webservice;

import org.mule.webservice.api.WebServiceInterface;
import org.mule.webservice.api.WebServiceRoute;

import java.util.List;

public abstract class AbstractWebServiceInterface implements WebServiceInterface
{
    private String name;

    private List<WebServiceRoute> routes;

    public AbstractWebServiceInterface(String name)
    {
        this.name = name;
    }

    @Override
    public List<WebServiceRoute> getRoutes()
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
