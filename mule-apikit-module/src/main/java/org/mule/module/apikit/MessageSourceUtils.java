/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.apache.log4j.Logger;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.extension.api.runtime.config.ConfigurationState;
import org.mule.runtime.extension.api.runtime.config.ConfiguredComponent;
import org.mule.runtime.extension.api.runtime.source.ParameterizedSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.prependIfMissing;
import static org.mule.apikit.common.CommonUtils.cast;
import static org.mule.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;

public class MessageSourceUtils {

  private static final Logger LOGGER = Logger.getLogger(MessageSourceUtils.class);

  /**
   * Extracts the configured HTTP URI from a flow. It only works for flows that uses the HTTP extension.
   *
  //     * @param flow where to extract the URI
   * @return the URI
   */
  public static URI getUriFromFlow(Component source) {
    if (source != null && isHttpExtensionSource(source)) {
      try {
        String resolvedPath = getListenerPath(source);
        return buildListenerUri(getConfigState(source).getConnectionParameters(), resolvedPath);
      } catch (Exception e) {
        LOGGER.warn("Error getting uri from flow " + source.getLocation().getRootContainerName(), e);
      }
    }

    return null;
  }

  private static String getListenerPath(Component source) {
    final ParameterizedSource parameterizedSource = cast(source);
    String listenerPath = cast(parameterizedSource.getInitialisationParameters().get("path"));
    final String basePath = cast(getConfigState(source).getConfigParameters().get("basePath"));
    listenerPath = prependIfMissing(listenerPath, "/");
    return basePath == null ? listenerPath : prependIfMissing(basePath, "/") + listenerPath;
  }

  private static ConfigurationState getConfigState(Component source) {
    final ConfiguredComponent configuredComponent = cast(source);
    return configuredComponent.getConfigurationInstance()
        .orElseThrow(() -> new RuntimeException("Source does not contain a configuration instance"))
        .getState();
  }

  private static boolean isHttpExtensionSource(Component source) {
    final ComponentIdentifier identifier = source.getLocation().getComponentIdentifier().getIdentifier();
    return identifier.equals(buildFromStringRepresentation("http:listener"));
  }

  private static URI buildListenerUri(Map<String, Object> connectionParams, String path)
      throws URISyntaxException {
    String host = cast(connectionParams.get("host"));
    Integer port = cast(connectionParams.get("port"));
    String scheme = connectionParams.get("protocol").toString().toLowerCase();
    return new URI(scheme, null, host, port, path, null, null);
  }

}
