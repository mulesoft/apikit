
package org.mule.webservice;

import org.mule.webservice.api.WebServiceOperation;
import org.mule.webservice.api.WebServiceInterface;

import java.util.List;

public abstract class AbstractWebServiceInterface implements WebServiceInterface
{
    private String name;
    private List<WebServiceOperation> operations;

    public AbstractWebServiceInterface(String name)
    {
        this.name = name;
    }

    @Override
    public List<WebServiceOperation> getOperations()
    {
        return operations;
    }

    @Override
    public String getName()
    {
        return name;
    }

}
