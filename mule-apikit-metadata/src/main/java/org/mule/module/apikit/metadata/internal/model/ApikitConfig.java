/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import java.util.List;
import java.util.Optional;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
//import org.mule.module.apikit.metadata.internal.amf.AmfHandler;
//import org.mule.module.apikit.metadata.internal.amf.AutoHandler;
import org.mule.module.apikit.metadata.internal.raml.RamlHandler;

import static org.mule.module.apikit.metadata.api.Metadata.MULE_APIKIT_PARSER;

class ApikitConfig {

  final private String name;
  final private String apiDefinition;
  final private List<FlowMapping> flowMappings;
  final private String httpStatusVarName;
  final private String outputHeadersVarName;
  final private String parser;
  final private ResourceLoader resourceLoader;
  final private Notifier notifier;

  private MetadataResolverFactory metadataResolverFactory = null;
  private Optional<MetadataResolver> metadataResolver = null;

  ApikitConfig(final String name, final String apiDefinition, List<FlowMapping> flowMappings,
               final String httpStatusVarName, final String outputHeadersVarName,
               final String parser, final ResourceLoader resourceLoader, final Notifier notifier) {

    this.name = name;
    this.apiDefinition = apiDefinition;
    this.flowMappings = flowMappings;
    this.httpStatusVarName = httpStatusVarName;
    this.outputHeadersVarName = outputHeadersVarName;
    this.parser = parser;
    this.metadataResolverFactory = metadataResolverFactory;
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  public String getName() {
    return name;
  }

  public List<FlowMapping> getFlowMappings() {
    return flowMappings;
  }

  public String getHttpStatusVarName() {
    return httpStatusVarName;
  }

  public String getOutputHeadersVarName() {
    return outputHeadersVarName;
  }

  public Optional<MetadataResolver> getMetadataResolver() {
    if (metadataResolver == null) {
      metadataResolver = getMetadataResolverFactory().getMetadataResolver(apiDefinition);
    }
    return metadataResolver;
  }

  private MetadataResolverFactory getMetadataResolverFactory() {
    if (metadataResolverFactory == null) {
      final String finalParser = getOverridedParser();
      metadataResolverFactory = new RamlHandler(resourceLoader, notifier);
      /*
      metadataResolverFactory = "RAML".equals(finalParser) ? new RamlHandler(resourceLoader, notifier)
          : "AMF".equals(finalParser) ? new AmfHandler(resourceLoader, notifier) : new AutoHandler(resourceLoader, notifier);
      */
    }
    return metadataResolverFactory;
  }

  private String getOverridedParser() {
    final String value = System.getProperty(MULE_APIKIT_PARSER, parser);
    return value == null ? "AUTO" : value;
  }
}
