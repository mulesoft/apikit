
package org.mule.webservice;

import org.mule.webservice.api.WebServiceRoute;
import org.mule.webservice.api.WebServiceInterface;

import java.util.List;

public abstract class AbstractWebServiceInterface implements WebServiceInterface
{
    private String name;
    private List<WebServiceRoute> operations;

    public AbstractWebServiceInterface(String name)
    {
        this.name = name;
    }

    @Override
    public List<WebServiceRoute> getRoutes()
    {
        return operations;
    }

    @Override
    public String getName()
    {
        return name;
    }

}
