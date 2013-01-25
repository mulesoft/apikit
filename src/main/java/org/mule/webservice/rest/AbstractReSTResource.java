
package org.mule.webservice.rest;

import java.util.List;

public abstract class AbstractReSTResource implements ReSTResource
{

    protected String name;

    protected List<ReSTAction> actions;
    protected ReSTProtocolAdaptor protocolAdaptor;

    @Override
    public String getName()
    {
        return name;
    }

}
