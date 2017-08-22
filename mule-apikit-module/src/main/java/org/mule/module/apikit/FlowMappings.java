/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.ArrayList;
import java.util.List;

public class FlowMappings {

  @Parameter
  private List<FlowMapping> flowMappings;

  public List<FlowMapping> getFlowMappings() {
    if (flowMappings == null) {
      return new ArrayList<>();
    }
    return flowMappings;
  }

  public void setFlowMappings(List<FlowMapping> flowMappings) {
    this.flowMappings = flowMappings;
  }
}
