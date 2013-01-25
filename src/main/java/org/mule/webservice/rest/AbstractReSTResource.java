
package org.mule.webservice.rest;

import org.mule.webservice.api.WebServiceOperation;

import java.util.List;

public abstract class AbstractReSTResource implements WebServiceOperation
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
