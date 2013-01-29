
package org.mule.webservice.rest;

import java.util.List;

public abstract class AbstractReSTResource implements ReSTResource
{

    protected String name;

    protected List<ReSTAction> actions;
    protected List<ReSTResource> resources;
    protected ReSTProtocolAdaptor protocolAdaptor;
    protected String templateUri;

    @Override
    public String getName()
    {
        return name;
    }

}
