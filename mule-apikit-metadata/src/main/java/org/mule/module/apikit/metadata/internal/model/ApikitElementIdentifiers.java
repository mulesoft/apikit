/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.config.internal.model.ComponentModel;

/**
 * Helper class that tells if a given XML Component is some of the valid APIKit
 * XML Elements.
 *
 * Related Clases:
 * {@link org.mule.module.apikit.metadata.internal.model.ApikitConfig}
 * {@link org.mule.module.apikit.metadata.internal.model.Flow}
 * {@link org.mule.module.apikit.metadata.internal.model.FlowMapping}
 */
public class ApikitElementIdentifiers {

  private ApikitElementIdentifiers() {}

  private static final ComponentIdentifier FLOW =
      ComponentIdentifier.buildFromStringRepresentation("flow");

  private static final ComponentIdentifier APIKIT_CONFIG =
      ComponentIdentifier.buildFromStringRepresentation("apikit:config");

  private static final ComponentIdentifier APIKIT_FLOW_MAPPINGS =
      ComponentIdentifier.buildFromStringRepresentation("apikit:flow-mappings");

  private static final ComponentIdentifier APIKIT_FLOW_MAPPING =
      ComponentIdentifier.buildFromStringRepresentation("apikit:flow-mapping");


  public static boolean isFlow(ComponentModel component) {
    return component.getIdentifier().equals(FLOW);
  }

  public static boolean isApikitConfig(ComponentModel component) {
    return component.getIdentifier().equals(APIKIT_CONFIG);
  }

  public static boolean isFlowMappings(ComponentIdentifier identifier) {
    return identifier.equals(APIKIT_FLOW_MAPPINGS);
  }

  public static boolean isFlowMapping(ComponentIdentifier identifier) {
    return identifier.equals(APIKIT_FLOW_MAPPING);
  }
}
