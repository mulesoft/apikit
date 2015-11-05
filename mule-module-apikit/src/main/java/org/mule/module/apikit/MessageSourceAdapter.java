/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.source.MessageSource;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.http.internal.listener.DefaultHttpListener;
import org.mule.module.http.internal.listener.DefaultHttpListenerConfig;

public class MessageSourceAdapter
{

    private IMessageSource delegate;

    public MessageSourceAdapter(MessageSource messageSource)
    {
        if (messageSource instanceof ImmutableEndpoint)
        {
            delegate = new MessageSourceEndpointAdapter((ImmutableEndpoint) messageSource);
        }
        else if (messageSource instanceof DefaultHttpListener)
        {
            delegate = new MessageSourceListenerAdapter((DefaultHttpListener) messageSource);
        }
        else if (messageSource == null)
        {
            throw new ApikitRuntimeException("Flow endpoint is null, APIKIT requires a listener ref in each of it's flows");
        }
        else
        {
            throw new ApikitRuntimeException("Message Source Type NOT SUPPORTED: " + messageSource.getClass());
        }

    }

    public String getAddress()
    {
        return delegate.getAddress();
    }

    public String getPath()
    {
        return delegate.getPath();
    }

    public String getScheme()
    {
        return delegate.getScheme();
    }

    private interface IMessageSource
    {
        String getAddress();
        String getPath();
        String getScheme();
    }

    private class MessageSourceEndpointAdapter implements IMessageSource
    {

        private ImmutableEndpoint endpoint;

        public MessageSourceEndpointAdapter(ImmutableEndpoint messageSource)
        {
            endpoint = messageSource;
        }

        @Override
        public String getAddress()
        {
            return endpoint.getAddress();
        }

        @Override
        public String getPath()
        {
            return endpoint.getEndpointURI().getPath();
        }

        @Override
        public String getScheme()
        {
            return endpoint.getEndpointURI().getScheme();
        }
    }

    private class MessageSourceListenerAdapter implements IMessageSource
    {

        private DefaultHttpListener listener;
        private DefaultHttpListenerConfig config;

        public MessageSourceListenerAdapter(DefaultHttpListener messageSource)
        {
            listener = messageSource;
            config = (DefaultHttpListenerConfig) messageSource.getConfig();
        }

        @Override
        public String getAddress()
        {
            return String.format("%s://%s:%s%s", getScheme(), config.getHost(), config.getPort(), getPath());
        }

        @Override
        public String getPath()
        {
            String path = listener.getPath();
            return path.endsWith("/*") ? path.substring(0, path.length() - 2) : path;
        }

        @Override
        public String getScheme()
        {
            return config.getTlsContext() != null ? "https" : "http";
        }
    }
}
