
package org.mule.webservice.rest;

import java.util.List;

public abstract class AbstractReSTResource implements ReSTResource
{

    protected String name;
    protected List<ReSTResource> resources;
    protected ReSTProtocolAdaptor protocolAdaptor;
    protected String templateUri;

    protected List<ReSTAction> actions;

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<ReSTAction> getActions()
    {
        return actions;
    }

    public void setActions(List<ReSTAction> actions)
    {
        this.actions = actions;
    }

}
