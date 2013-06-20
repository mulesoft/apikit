package apikit2;

import static org.mule.transport.http.HttpConnector.HTTP_STATUS_PROPERTY;

import org.mule.api.MuleEvent;
import org.mule.exception.CatchMessagingExceptionStrategy;

import java.util.ArrayList;
import java.util.List;

public class MappingExceptionListener extends CatchMessagingExceptionStrategy
{
    private int statusCode;
    private List<String> exceptions = new ArrayList<String>();

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public void setExceptions(List<String> exceptions)
    {
        this.exceptions = exceptions;
    }

    @Override
    public boolean accept(MuleEvent event)
    {
        Throwable rootException = event.getMessage().getExceptionPayload().getRootException();
        String name = rootException.getClass().getName();
        if (exceptions.contains(name))
        {
            return true;
        }
        return false;
    }

    @Override
    protected MuleEvent afterRouting(Exception exception, MuleEvent event)
    {
        event.getMessage().setOutboundProperty(HTTP_STATUS_PROPERTY, statusCode);
        return event;
    }

    @Override
    public String toString()
    {
        return "MappingExceptionListener{" +
               "statusCode=" + statusCode +
               ", exceptions=" + exceptions +
               '}';
    }
}
