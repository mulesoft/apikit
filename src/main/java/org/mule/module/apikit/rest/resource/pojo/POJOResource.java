
package org.mule.module.apikit.rest.resource.pojo;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractRestResource;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.transport.http.HttpConnector;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationFactory;

import java.io.InputStream;
import java.net.URI;
import java.util.EnumSet;
import java.util.Set;

public class POJOResource extends AbstractRestResource implements Initialisable
{
    protected WebApplication application;
    protected Object resource;

    public POJOResource(String name, RestResource parentResource)
    {
        super(name, parentResource);
    }

    @Override
    public void initialise() throws InitialisationException
    {
        application = WebApplicationFactory.createWebApplication();
        application.initiate(new DefaultResourceConfig(resource.getClass()),
            new IoCComponentProviderFactory()
            {
                @Override
                public IoCComponentProvider getComponentProvider(ComponentContext cc, Class<?> c)
                {
                    return new IoCInstantiatedComponentProvider()
                    {
                        @Override
                        public Object getInstance()
                        {
                            return resource;
                        }

                        @Override
                        public Object getInjectableInstance(Object o)
                        {
                            // TODO Auto-generated method stub
                            return null;
                        }
                    };
                }

                @Override
                public IoCComponentProvider getComponentProvider(Class<?> c)
                {
                    return new IoCInstantiatedComponentProvider()
                    {
                        @Override
                        public Object getInstance()
                        {
                            return resource;
                        }

                        @Override
                        public Object getInjectableInstance(Object o)
                        {
                            // TODO Auto-generated method stub
                            return null;
                        }
                    };
                }
            });
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.UPDATE);
    }

    @Override
    public void handle(RestRequest restRequest) throws RestException
    {
        try
        {
            MuleEvent event = restRequest.getMuleEvent();
            MuleMessage message = event.getMessage();

            String path = (String) message.getInboundProperty(HttpConnector.HTTP_REQUEST_PROPERTY);
            String contextPath = (String) message.getInboundProperty(HttpConnector.HTTP_CONTEXT_PATH_PROPERTY);
            String query = null;
            int queryIdx = path.indexOf('?');
            if (queryIdx != -1)
            {
                query = path.substring(queryIdx + 1);
                path = path.substring(0, queryIdx);
            }

            URI endpointUri = event.getMessageSourceURI();
            String host = message.getInboundProperty("Host", endpointUri.getHost());
            String method = message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY);
            InBoundHeaders headers = new InBoundHeaders();
            for (Object prop : message.getInboundPropertyNames())
            {
                Object property = message.getInboundProperty(prop.toString());
                if (property != null)
                {
                    headers.add(prop.toString(), property.toString());
                }
            }

            String scheme;
            if ("servlet".equals(endpointUri.getScheme()))
            {
                scheme = "http";
            }
            else
            {
                scheme = endpointUri.getScheme();
            }

            ContainerRequest req = new ContainerRequest(application, method, restRequest.getProtocolAdaptor()
                .getURI(), restRequest.getProtocolAdaptor().getURI(), headers,
                message.getPayload(InputStream.class));
            if (logger.isDebugEnabled())
            {
                logger.debug("Base URI: " + restRequest.getProtocolAdaptor().getBaseURI());
                logger.debug("Complete URI: " + restRequest.getProtocolAdaptor().getURI());
            }

            MuleResponseWriter writer = new MuleResponseWriter(message);
            ContainerResponse res = new ContainerResponse(application, req, writer);

            application.handleRequest(req, res);

            event.getMessage().setPayload(writer.getResponse());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setResource(Object resource)
    {
        this.resource = resource;
    }
}
