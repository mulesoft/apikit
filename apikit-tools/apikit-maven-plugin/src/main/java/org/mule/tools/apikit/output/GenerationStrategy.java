/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.apache.maven.plugin.logging.Log;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.input.APIDiff;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;

import java.util.Set;

public class GenerationStrategy {
    private Log log;

    public GenerationStrategy(Log log) {
        this.log = log;
    }

    public Set<ResourceActionMimeTypeTriplet> generate(RAMLFilesParser RAMLFilesParser,
                                  MuleConfigParser muleConfigParser) {
        Set<API> apisInMuleConfigs = muleConfigParser.getIncludedApis();
        Set<ResourceActionMimeTypeTriplet> yamlEntries = RAMLFilesParser.getEntries();
        Set<ResourceActionMimeTypeTriplet> muleFlowEntries = muleConfigParser.getEntries();
        if (apisInMuleConfigs.isEmpty()) {
            if (yamlEntries.isEmpty()) {
                // No APIs No Flow APIs
                log.info("No APIs or APIKit flows found.");
            } else
                log.info("Generating apikit:flows for the following operations: " + yamlEntries);

            return yamlEntries;
        } else {
            if (yamlEntries.isEmpty()) {
                // there are implemented APIs without a YAML file. NOMB.
                log.warn("The following apikit:flows do not match any RAML API binding: " + apisInMuleConfigs);
                return yamlEntries;
            } else {
                Set<ResourceActionMimeTypeTriplet> flowEntries1 = new APIDiff(yamlEntries, muleFlowEntries).getEntries();
                log.info("Adding new apikit:flows to existing files for the following operations: " + flowEntries1);
                return flowEntries1;

            }
        }
    }
}
