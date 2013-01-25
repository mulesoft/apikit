
package org.mule.webservice;

import org.mule.webservice.api.ServiceOperation;
import org.mule.webservice.api.WebServiceInterface;

import java.util.List;

public abstract class AbstractWebServiceInterface implements WebServiceInterface
{
    private String name;
    private List<ServiceOperation> operations;

    public AbstractWebServiceInterface(String name)
    {
        this.name = name;
    }

    @Override
    public List<ServiceOperation> getOperations()
    {
        return operations;
    }

    @Override
    public String getName()
    {
        return name;
    }

}
