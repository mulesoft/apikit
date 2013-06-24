package apikit2;

import static org.mule.transport.http.HttpConnector.HTTP_STATUS_PROPERTY;

import org.mule.api.MuleEvent;
import org.mule.exception.CatchMessagingExceptionStrategy;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

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
        Throwable exception = event.getMessage().getExceptionPayload().getException();
        Map<Throwable, Object> visited = new IdentityHashMap<Throwable, Object>();
        while (exception != null && !visited.containsKey(exception))
        {
            String name = exception.getClass().getName();
            if (exceptions.contains(name))
            {
                return true;
            }
            visited.put(exception, null);
            exception = exception.getCause();
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
