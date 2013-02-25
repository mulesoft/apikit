
package org.mule.module.apikit.rest.resource;

import static org.mule.module.apikit.rest.operation.RestOperationType.EXISTS;
import static org.mule.module.apikit.rest.operation.RestOperationType.RETRIEVE;

import org.mule.api.MuleEvent;
import org.mule.api.expression.ExpressionManager;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.apikit.UnauthorizedException;
import org.mule.module.apikit.api.WebServiceRoute;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.OperationNotAllowedException;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.transport.NullPayload;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRestResource implements RestResource
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String name;
    protected String description = "";
    protected List<RestOperation> operations = new ArrayList<RestOperation>();
    protected String accessExpression;

    public AbstractRestResource(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public List<RestOperation> getOperations()
    {
        return operations;
    }

    public void setOperations(List<RestOperation> operations)
    {
        this.operations = operations;
        for (RestOperation operation : operations)
        {
            if (operation instanceof AbstractRestOperation)
            {
                ((AbstractRestOperation) operation).setResource(this);
            }
        }
    }

    private RestOperation getOperation(RestOperationType operationType)
    {
        RestOperation action = null;
        for (RestOperation a : getOperations())
        {
            if (a.getType() == operationType)
            {
                action = a;
                break;
            }
        }
        return action;
    }

    protected RestOperation getAction(RestOperationType actionType, MuleEvent muleEvent)
        throws OperationNotAllowedException
    {
        if (!getSupportedActionTypes().contains(actionType))
        {
            throw new OperationNotAllowedException(this, actionType);
        }
        RestOperation action = getOperation(actionType);
        if (action == null && EXISTS == actionType)
        {
            action = useRetrieveAsExists();
        }
        if (action == null)
        {
            throw new OperationNotAllowedException(this, actionType);
        }
        return action;
    }

    private RestOperation useRetrieveAsExists()
    {
        RestOperation retrieve = getOperation(RETRIEVE);
        if (retrieve == null)
        {
            return null;
        }
        return new ExistsByRetrieveOperation(retrieve);
    }

    @Override
    public boolean isActionTypeAllowed(RestOperationType actionType)
    {
        for (RestOperation operation : operations)
        {
            if (operation.getType().equals(actionType))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<RestOperationType> getAllowedActionTypes()
    {
        Set<RestOperationType> allowedTypes = new HashSet<RestOperationType>();
        for (RestOperation operation : operations)
        {
            allowedTypes.add(operation.getType());
        }
        return allowedTypes;
    }

    @Override
    public MuleEvent handle(RestRequest restCall) throws RestException
    {
        return processResource(restCall);
    }

    protected MuleEvent processResource(RestRequest request) throws RestException
    {
        try
        {
            authorize(request);
            this.getAction(request.getProtocolAdaptor().getOperationType(), request.getMuleEvent()).handle(
                request);
            if (RestOperationType.EXISTS == request.getProtocolAdaptor().getOperationType())
            {
                request.getMuleEvent().getMessage().setPayload(NullPayload.getInstance());
            }
        }
        catch (RestException rana)
        {
            request.getProtocolAdaptor().handleException(rana, request);
        }
        return request.getMuleEvent();
    }

    @Override
    public String getAccessExpression()
    {
        return accessExpression;
    }

    public void setAccessExpression(String accessExpression)
    {
        this.accessExpression = accessExpression;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public List<RestOperation> getAuthorizedActions(RestRequest request)
    {
        List<RestOperation> result = new ArrayList<RestOperation>();
        for (RestOperation action : getOperations())
        {
            if (isAuthorized(action, request))
            {
                result.add(action);
            }
        }
        return result;
    }

    protected void authorize(RestRequest request) throws UnauthorizedException
    {
        if (!isAuthorized(this, request))
        {
            throw new UnauthorizedException(this);
        }
    }

    protected boolean isAuthorized(WebServiceRoute route, RestRequest request)
    {
        ExpressionManager expManager = request.getMuleEvent().getMuleContext().getExpressionManager();

        if (route.getAccessExpression() == null
            || expManager.evaluateBoolean(route.getAccessExpression(), request.getMuleEvent()))
        {
            return true;
        }
        return false;
    }

    protected abstract Set<RestOperationType> getSupportedActionTypes();

    static class ExistsByRetrieveOperation extends AbstractRestOperation
    {

        private RestOperation retrieve;

        public ExistsByRetrieveOperation(RestOperation retrieve)
        {
            this.retrieve = retrieve;
            this.type = EXISTS;
        }

        @Override
        public MessageProcessor getHandler()
        {
            return retrieve.getHandler();
        }
    }
}
