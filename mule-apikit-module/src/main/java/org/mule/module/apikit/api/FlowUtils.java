/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.core.api.construct.Flow;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.mule.apikit.common.CommonUtils.cast;
import static org.mule.module.apikit.MessageSourceUtils.getUriFromFlow;

public class FlowUtils {

  private static final String FLOW_TAG_NAME = "flow";
  private static final String MULE_NAMESPACE = "mule";

  public static List<Flow> getFlowsList(ConfigurationComponentLocator locator) {
    return cast(locator.find(ComponentIdentifier.builder().name(FLOW_TAG_NAME).namespace(MULE_NAMESPACE).build()));
  }

  public static Optional<Component> getSource(ConfigurationComponentLocator locator, String flowName) {
    return locator.find(Location.builder().globalName(flowName).addSourcePart().build());
  }

  public static Optional<URI> getSourceLocation(ConfigurationComponentLocator locator, String flowName) {
    return getSource(locator, flowName).flatMap(source -> ofNullable(getUriFromFlow(source)));
  }

}
