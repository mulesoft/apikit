/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.model;

import org.mule.module.apikit.metadata.interfaces.Notifier;
import org.mule.module.apikit.metadata.raml.RamlApiWrapper;
import org.mule.raml.interfaces.model.IRaml;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ApikitConfig {

  final private String name;
  final private String raml;
  final private List<FlowMapping> flowMappings;
  final private Supplier<Optional<IRaml>> apiSupplier;
  final private String httpStatusVarName;
  final private String outputHeadersVarName;
  final private Notifier notifier;
  private Optional<RamlApiWrapper> ramlApi;

  public ApikitConfig(String name, String raml, List<FlowMapping> flowMappings, Supplier<Optional<IRaml>> apiSupplier,
                      String httpStatusVarName, String outputHeadersVarName, Notifier notifier) {
    this.name = name;
    this.raml = raml;
    this.flowMappings = flowMappings;
    this.apiSupplier = apiSupplier;
    this.httpStatusVarName = httpStatusVarName;
    this.outputHeadersVarName = outputHeadersVarName;
    this.notifier = notifier;
  }

  public String getName() {
    return name;
  }

  public String getRaml() {
    return raml;
  }

  public List<FlowMapping> getFlowMappings() {
    return flowMappings;
  }

  public Optional<RamlApiWrapper> getApi() {
    if (ramlApi == null) {
      ramlApi = apiSupplier.get().map(api -> new RamlApiWrapper(api, notifier));
    }
    return ramlApi;
  }

  public String getHttpStatusVarName() {
    return httpStatusVarName;
  }

  public String getOutputHeadersVarName() {
    return outputHeadersVarName;
  }
}
