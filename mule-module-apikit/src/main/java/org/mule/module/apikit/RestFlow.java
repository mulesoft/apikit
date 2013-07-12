package org.mule.module.apikit;

import org.mule.api.MuleContext;
import org.mule.construct.Flow;

public class RestFlow extends Flow
{

    private String resource;
    private String action;

    public RestFlow(String name, MuleContext muleContext)
    {
        super(name, muleContext);
    }

    public void setResource(String resource)
    {
        this.resource = resource;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getResource()
    {
        return resource;
    }

    public String getAction()
    {
        return action;
    }
}
