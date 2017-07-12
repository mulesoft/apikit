/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.metadata;

import org.mule.metadata.api.model.FunctionType;
import org.mule.module.metadata.interfaces.ResourceLoader;
import org.mule.runtime.config.spring.dsl.model.ApplicationModel;

import java.util.Optional;

public class Metadata
{
    private MetadataHandler metadataHandler;

    private Metadata(ApplicationModel applicationModel, ResourceLoader resourceLoader) {
        init(applicationModel, resourceLoader);
    }

    public Optional<FunctionType> getMetadataForFlow(String flowName) {
        return metadataHandler.getMetadataForFlow(flowName);
    }

    private void init(ApplicationModel applicationModel, ResourceLoader resourceLoader) {
        metadataHandler = new MetadataHandler(applicationModel, resourceLoader);
    }


    /**
     * Builder for Metadata module
     */
    public static class Builder {

        private ResourceLoader resourceLoader;
        private ApplicationModel applicationModel;

        public Builder () {

        }

        public Builder withResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
            return this;
        }

        public Builder withApplicationModel(ApplicationModel applicationModel) {
            this.applicationModel = applicationModel;
            return this;
        }

        public Metadata build() {
            return new Metadata(applicationModel, resourceLoader);
        }

    }

}