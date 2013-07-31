package org.mule.module.apikit;

import org.mule.construct.Flow;

public class FlowMapping
{

    private String resource;
    private String action;
    private Flow flow;

    public String getResource()
    {
        return resource;
    }

    public void setResource(String resource)
    {
        this.resource = resource;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public Flow getFlow()
    {
        return flow;
    }

    public void setFlow(Flow flow)
    {
        this.flow = flow;
    }
}
