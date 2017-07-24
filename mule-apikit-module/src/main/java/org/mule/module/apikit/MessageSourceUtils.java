/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;
import static org.apache.commons.lang3.reflect.FieldUtils.readField;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.source.MessageSource;
import org.mule.runtime.core.internal.connection.ConnectionProviderWrapper;
import org.mule.runtime.core.internal.connection.ReconnectableConnectionProviderWrapper;
import org.mule.runtime.extension.api.runtime.ConfigurationInstance;
import org.mule.runtime.extension.api.runtime.ConfigurationProvider;
import org.mule.runtime.module.extension.internal.runtime.ExtensionComponent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

public class MessageSourceUtils {

    private static final Logger LOGGER = Logger.getLogger(MessageSourceUtils.class);

    /**
     * TODO super hack cause when flow is initialised it starts appending the base path
     * TODO to the listener one, OMG
     **/
    public static URI getUriFromFlowWhenHTTPIsInitialised(Flow flow) {
        if (isHttpExtensionSource(flow.getSource())) {
            try {

                ExtensionComponent httpExtensionMessageSource = (ExtensionComponent) flow.getSource();
                ConfigurationInstance httpConfiguration = getConfiguration(httpExtensionMessageSource);
                String listenerPath = getListenerPath(httpExtensionMessageSource);
                return buildListenerUri(getConnectionParams(httpConfiguration), listenerPath);
            } catch (Exception e) {
            }
        }

        return null;
    }

    /**
     * Extracts the configured HTTP URI from a flow. It only works for flows that uses the HTTP extension.
     *
     * @param flow where to extract the URI
     * @return the URI
     */
    public static URI getUriFromFlow(Flow flow) {
        if (isHttpExtensionSource(flow.getSource())) {
            try {

                ExtensionComponent httpExtensionMessageSource = (ExtensionComponent) flow.getSource();
                ConfigurationInstance httpConfiguration = getConfiguration(httpExtensionMessageSource);
                String listenerPath = getListenerPath(httpExtensionMessageSource);
                return buildListenerUri(getConnectionParams(httpConfiguration), getResolvedPath(httpConfiguration, listenerPath));
            } catch (Exception e) {
                LOGGER.warn("Error getting uri from flow " + flow.getName(), e);
            }
        }

        return null;
    }

    private static String getListenerPath(ExtensionComponent httpExtensionMessageSource) throws IllegalAccessException {
        Object object = readField(httpExtensionMessageSource, "sourceAdapter", true);
        Object source = readField(object, "source", true);
        return (String) readField(source, "path", true);
    }

    private static String getResolvedPath(ConfigurationInstance configurationInstance, String listenerPath)
            throws IllegalAccessException {

        Object httpListenerConfig = configurationInstance.getValue();
        String basePath = (String) readField(httpListenerConfig, "basePath", true);
        listenerPath = listenerPath.startsWith("/") ? listenerPath : "/" + listenerPath;
        return basePath == null ? listenerPath : basePath + listenerPath;
    }

    private static ConfigurationInstance getConfiguration(ExtensionComponent httpExtensionMessageSource)
            throws IllegalAccessException {
        AtomicReference<ConfigurationProvider> configProvider =
                (AtomicReference<ConfigurationProvider>) readField(httpExtensionMessageSource, "configurationProvider", true);

        return configProvider.get().get(null);
    }

    private static Object getConnectionParams(ConfigurationInstance configurationInstance)
            throws IllegalAccessException {

        Optional<ConnectionProvider> connectionProvider = configurationInstance.getConnectionProvider();
        if (connectionProvider.isPresent()) {
            ConnectionProvider providerDelegate = ((ConnectionProviderWrapper) connectionProvider.get()).getDelegate();
            ReconnectableConnectionProviderWrapper reconnectableConnectionProviderWrapper =
                    (ReconnectableConnectionProviderWrapper) providerDelegate;

            ConnectionProvider delegate = reconnectableConnectionProviderWrapper.getDelegate();
            return readField(delegate, "connectionParams", true);
        }

        throw new IllegalArgumentException("HTTP extension message source does not have a connection provider");
    }

    private static boolean isHttpExtensionSource(MessageSource messageSource) {
        if (messageSource instanceof ExtensionComponent) {
            ExtensionComponent extensionMessageSource = (ExtensionComponent) messageSource;
            return "HTTP".equals(extensionMessageSource.getExtensionModel().getName());
        }

        return false;
    }

    private static URI buildListenerUri(Object connectionParams, String resolvedPath)
            throws URISyntaxException, IllegalAccessException {
        String host = (String) readField(connectionParams, "host", true);
        Integer port = (Integer) readField(connectionParams, "port", true);
        Object protocol = readField(connectionParams, "protocol", true);
        String scheme = (String) readField(protocol, "scheme", true);
        return new URI(scheme, null, host, port, resolvedPath, null, null);
    }
}