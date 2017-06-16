/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.mule.tools.apikit.input.APIDiff;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;

public class GenerationStrategy {
    private Log log;

    public GenerationStrategy(Log log) {
        this.log = log;
    }

    public List<GenerationModel> generate(RAMLFilesParser RAMLFilesParser,
                                  MuleConfigParser muleConfigParser) {
        Set<API> apisInMuleConfigs = muleConfigParser.getIncludedApis();
        Set<ResourceActionMimeTypeTriplet> ramlEntries = RAMLFilesParser.getEntries().keySet();
        Set<ResourceActionMimeTypeTriplet> muleFlowEntries = muleConfigParser.getEntries();
        List<GenerationModel> generationModels = new ArrayList<GenerationModel>();

        if (apisInMuleConfigs.isEmpty()) {
            if (ramlEntries.isEmpty()) {
                // No APIs No Flow APIs
                log.info("No APIs or APIKit flows found.");
            } else {
                log.info("Generating apikit:flows for the following operations: " + ramlEntries);
            }
            generationModels.addAll(RAMLFilesParser.getEntries().values());
        } else {
            if (ramlEntries.isEmpty()) {
                // there are implemented APIs without a RAML file. NOMB.
                String xmlFilesWithoutRaml = "";

                for (API api : apisInMuleConfigs)
                {
                    xmlFilesWithoutRaml = xmlFilesWithoutRaml + " " + api.getXmlFile().getAbsolutePath();
                }
                log.warn("The following apikit:flows do not match any RAML API binding: " + xmlFilesWithoutRaml);

                generationModels.addAll(RAMLFilesParser.getEntries().values());
            } else {
                Set<ResourceActionMimeTypeTriplet> diffTriplets = new APIDiff(ramlEntries, muleFlowEntries).getEntries();
                log.info("Adding new apikit:flows to existing files for the following operations: " + diffTriplets);

                for (ResourceActionMimeTypeTriplet entry : diffTriplets) {
                    if (RAMLFilesParser.getEntries().containsKey(entry)) {
                        generationModels.add(RAMLFilesParser.getEntries().get(entry));
                    }
                }
            }
        }

        Collections.sort(generationModels);
        return generationModels;
    }
}
