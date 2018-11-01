/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.api;

import java.util.Optional;
import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.internal.model.MetadataModel;
import org.mule.runtime.config.internal.model.ApplicationModel;

import static org.mule.apikit.common.APISyncUtils.isExchangeModules;
import static org.mule.apikit.common.APISyncUtils.toApiSyncResource;

public interface Metadata {

  String MULE_APIKIT_PARSER = "mule.apikit.parser";

  /**
  * Gets the metadata for a Flow
  * @param flowName Name of the flow
  * @return The Metadata
  */
  public Optional<FunctionType> getMetadataForFlow(final String flowName);

  /**
   * Builder for Metadata module
   */
  class Builder {

    private ResourceLoader resourceLoader;
    private ApplicationModel applicationModel;
    private Notifier notifier;

    public Builder() {

    }

    public Builder withResourceLoader(final ResourceLoader resourceLoader) {
      this.resourceLoader = resourceLoader;
      return this;
    }

    public Builder withApplicationModel(final ApplicationModel applicationModel) {
      this.applicationModel = applicationModel;
      return this;
    }

    public Builder withNotifier(final Notifier notifier) {
      this.notifier = notifier;
      return this;
    }

    public Metadata build() {
      return new MetadataModel(applicationModel, doMagic(resourceLoader), notifier);
    }

    private static ResourceLoader doMagic(final ResourceLoader resourceLoader) {
      return s -> {
        if (isExchangeModules(s)) {
          String apiSyncResource = toApiSyncResource(s);
          if (apiSyncResource != null)
            return resourceLoader.getResource(apiSyncResource);
        }
        return resourceLoader.getResource(s);
      };
    }
  }
}
