
package org.mule.webservice;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.webservice.api.WebServiceInterface;

import java.util.List;

public class AbstractWebServiceInterface<T> implements WebServiceInterface
{
    private String name;
    private List<T> operations;

    public AbstractWebServiceInterface(String name)
    {
        this.name = name;
    }

    @Override
    public List<T> getOperations()
    {
        return operations;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
