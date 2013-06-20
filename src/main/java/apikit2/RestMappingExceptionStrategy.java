package apikit2;

import org.mule.api.GlobalNameableObject;
import org.mule.api.MuleEvent;
import org.mule.api.MuleRuntimeException;
import org.mule.api.exception.MessagingExceptionHandlerAcceptor;
import org.mule.config.i18n.CoreMessages;
import org.mule.message.DefaultExceptionPayload;
import org.mule.processor.AbstractMuleObjectOwner;

import java.util.List;

public class RestMappingExceptionStrategy extends AbstractMuleObjectOwner<MappingExceptionListener>
        implements MessagingExceptionHandlerAcceptor, GlobalNameableObject
{

    private List<MappingExceptionListener> exceptionListeners;

    protected String globalName;

    @Override
    public MuleEvent handleException(Exception exception, MuleEvent event)
    {
        event.getMessage().setExceptionPayload(new DefaultExceptionPayload(exception));
        for (MessagingExceptionHandlerAcceptor exceptionListener : exceptionListeners)
        {
            if (exceptionListener.accept(event))
            {
                event.getMessage().setExceptionPayload(null);
                return exceptionListener.handleException(exception, event);
            }
        }
        throw new MuleRuntimeException(CoreMessages.createStaticMessage("Default exception strategy must accept any event."));
    }

    @Override
    public boolean accept(MuleEvent event)
    {
        return true;
    }

    @Override
    public boolean acceptsAll()
    {
        return true;
    }


    public void setExceptionListeners(List<MappingExceptionListener> exceptionListeners)
    {
        this.exceptionListeners = exceptionListeners;
    }

    @Override
    public String getGlobalName()
    {
        return globalName;
    }

    @Override
    public void setGlobalName(String globalName)
    {
        this.globalName = globalName;
    }

    @Override
    protected List<MappingExceptionListener> getOwnedObjects()
    {
        return exceptionListeners;
    }


}
