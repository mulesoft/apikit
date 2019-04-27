/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlowName {

  private FlowName() {}

  public static final String FLOW_NAME_SEPARATOR = ":";
  public static final String URL_RESOURCE_SEPARATOR = "/";

  private static final ImmutableMap<String, String> specialCharacters = ImmutableMap.<String, String>builder()
      .put(URL_RESOURCE_SEPARATOR, "\\")
      .put("{", "(")
      .put("}", ")")
      .build();

  private static final String APIKIT_FLOW_NAME_FORMAT = "^([^:]+):(" + URL_RESOURCE_SEPARATOR + "[^:]*)(:([^:]+))?(:(.*))?$";

  private static final Pattern PATTERN = Pattern.compile(APIKIT_FLOW_NAME_FORMAT);

  public static Matcher getMatcher(String flowName) {
    if (isEmpty(flowName)) {
      throw new IllegalArgumentException("Flow name cannot be null or empty");
    }
    Matcher flowNameMatcher = PATTERN.matcher(flowName);
    if (!flowNameMatcher.find()) {
      throw new IllegalArgumentException("Invalid apikit flow name, expected format is: action:resource[:config]");
    }
    return flowNameMatcher;
  }

  public static String encode(String value) {
    for (Map.Entry<String, String> entry : specialCharacters.entrySet()) {
      value = value.replace(entry.getKey(), entry.getValue());
    }

    return value;
  }

  public static String decode(String value) {
    for (Map.Entry<String, String> entry : specialCharacters.entrySet()) {
      value = value.replace(entry.getValue(), entry.getKey());
    }

    return value;
  }

  public static String getAction(String flowName) {
    return getAction(getMatcher(flowName));
  }

  public static String getAction(Matcher flowNameMatcher) {
    return flowNameMatcher.group(1);
  }

  public static String getResource(String flowName) {
    return getResource(getMatcher(flowName));
  }

  public static String getResource(Matcher flowNameMatcher) {
    return flowNameMatcher.group(2);
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

}
