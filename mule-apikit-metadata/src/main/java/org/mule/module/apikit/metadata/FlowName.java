/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.apache.commons.lang.StringUtils;
import org.mule.raml.interfaces.model.IActionType;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlowName {

  private FlowName() {}

  public static final String FLOW_NAME_SEPARATOR = ":";
  public static final String URL_RESOURCE_SEPARATOR = "/";
  public static final String RESOURCE_SEPARATOR = "\\";
  private static final String APIKIT_FLOW_NAME_FORMAT = "^([^:]+):(" + URL_RESOURCE_SEPARATOR + "[^:]*)(:([^:]+))?(:(.*))?$";

  private static Pattern flowNamePattern = Pattern.compile(APIKIT_FLOW_NAME_FORMAT);

  public static Matcher getMatcher(String flowName) {
    if (StringUtils.isEmpty(flowName)) {
      throw new IllegalArgumentException("Flow name cannot be null or empty");
    }

    Matcher flowNameMatcher = flowNamePattern.matcher(flowName);

    if (!flowNameMatcher.find()) {
      throw new IllegalArgumentException("Invalid apikit flow name, expected format is: action:resource[:config]");
    }

    return flowNameMatcher;
  }

  public static String encode(String resource) {
    return resource.replace(URL_RESOURCE_SEPARATOR, RESOURCE_SEPARATOR);
  }

  public static String decode(String resource) {
    return resource.replace(RESOURCE_SEPARATOR, URL_RESOURCE_SEPARATOR);
  }

  public static String getAction(String flowName) {
    return getAction(getMatcher(flowName));
  }

  public static String getAction(Matcher flowNameMatcher) {
    final String action = flowNameMatcher.group(1);

    if (!isValidAction(action)) {
      throw new IllegalArgumentException(action + " is not a valid action type");
    }

    return action;
  }

  public static String getResource(String flowName) {
    return getResource(getMatcher(flowName));
  }

  public static String getResource(Matcher flowNameMatcher) {
    return flowNameMatcher.group(2);
  }

  public static Optional<String> getMimeType(String flowName, Collection<String> existingConfigs) {
    return getMimeType(getMatcher(flowName), existingConfigs);
  }

  public static Optional<String> getMimeType(Matcher flowNameMatcher, Collection<String> existingConfigs) {
    if (flowNameMatcher.group(4) != null) {
      if (flowNameMatcher.group(6) == null) {
        if (existingConfigs == null || !existingConfigs.contains(flowNameMatcher.group(4))) {
          return Optional.of(flowNameMatcher.group(4));
        }
      } else {
        return Optional.of(flowNameMatcher.group(4));
      }
    }

    return Optional.empty();
  }

  public static Optional<String> getConfig(String flowName, Collection<String> existingConfigs) {
    return getConfig(getMatcher(flowName), existingConfigs);
  }

  public static Optional<String> getConfig(Matcher flowNameMatcher, Collection<String> existingConfigs) {
    if (flowNameMatcher.group(4) != null) {
      if (flowNameMatcher.group(6) == null) {
        if (existingConfigs != null && existingConfigs.contains(flowNameMatcher.group(4))) {
          return Optional.of(flowNameMatcher.group(4));
        }
      } else {
        return Optional.of(flowNameMatcher.group(6));
      }
    }

    return Optional.empty();
  }

  private static boolean isValidAction(String name) {
    for (IActionType actionType : IActionType.values()) {
      if (actionType.toString().equals(name.toUpperCase())) {
        return true;
      }
    }
    return false;
  }
}
