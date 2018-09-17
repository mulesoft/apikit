/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import static java.lang.String.format;
import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.interfaces.Notifier;
import org.mule.module.apikit.metadata.interfaces.ResourceLoader;
import org.mule.module.apikit.metadata.raml.RamlHandler;
import org.mule.runtime.config.internal.model.ApplicationModel;

import java.util.Optional;

public class Metadata {

  private MetadataHandler metadataHandler;

  private Metadata(ApplicationModel applicationModel, ResourceLoader resourceLoader, Notifier notifier) {
    init(applicationModel, resourceLoader, notifier);
  }

  private void init(ApplicationModel applicationModel, ResourceLoader resourceLoader, Notifier notifier) {

    final RamlHandler ramlHandler = new RamlHandler(resourceLoader, notifier);
    final ApplicationModelWrapper wrapper = new ApplicationModelWrapper(applicationModel, ramlHandler, notifier);
    metadataHandler = new MetadataHandler(wrapper, notifier);
  }

  /**
   * Gets the metadata for a Flow
   * @param flowName Name of the flow
   * @return The Metadata
   */
  public Optional<FunctionType> getMetadataForFlow(String flowName) {
    return metadataHandler.getMetadataForFlow(flowName);
  }


  /**
   * Builder for Metadata module
   */
  public static class Builder {

    private static final String RESOURCE_FORMAT = "resource::%s:%s:%s:%s:%s:%s";

    private ResourceLoader resourceLoader;
    private ApplicationModel applicationModel;
    private Notifier notifier;

    public Builder() {

    }

    public Builder withResourceLoader(ResourceLoader resourceLoader) {
      this.resourceLoader = resourceLoader;
      return this;
    }

    public Builder withApplicationModel(ApplicationModel applicationModel) {
      this.applicationModel = applicationModel;
      return this;
    }

    public Builder withNotifier(Notifier notifier) {
      this.notifier = notifier;
      return this;
    }

    public Metadata build() {
      return new Metadata(applicationModel, s -> {
        if (s.startsWith("/exchange_modules") || s.startsWith("exchange_modules")) {
          String[] resourceParts = s.split("/");
          int length = resourceParts.length;
          return resourceLoader.getRamlResource(format(RESOURCE_FORMAT, resourceParts[length - 6], resourceParts[length - 5],
                                                       resourceParts[length - 4], resourceParts[length - 3],
                                                       resourceParts[length - 2], resourceParts[length - 1]));
        }
        return resourceLoader.getRamlResource(s);
      }, notifier);

    }

  }

}
