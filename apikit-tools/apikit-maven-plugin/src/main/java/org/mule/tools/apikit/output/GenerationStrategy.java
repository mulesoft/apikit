package org.mule.tools.apikit.output;

import org.apache.maven.plugin.logging.Log;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.input.APIDiff;
import org.mule.tools.apikit.model.ResourceActionPair;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;

import java.util.Set;

public class GenerationStrategy {
    private Log log;

    public GenerationStrategy(Log log) {
        this.log = log;
    }

    public Set<ResourceActionPair> generate(RAMLFilesParser RAMLFilesParser,
                                  MuleConfigParser muleConfigParser) {
        Set<API> apisInMuleConfigs = muleConfigParser.getIncludedApis();
        Set<ResourceActionPair> yamlEntries = RAMLFilesParser.getEntries();
        Set<ResourceActionPair> muleFlowEntries = muleConfigParser.getEntries();
        if (apisInMuleConfigs.isEmpty()) {
            if (yamlEntries.isEmpty()) {
                // No APIs No Flow APIs
                log.info("No APIs or APIKit flows found.");
                return yamlEntries;
            } else {
                log.info("Generating apikit:flows for the following operations: " + yamlEntries);
                return yamlEntries;
            }
        } else {
            if (yamlEntries.isEmpty()) {
                // there are implemented APIs without a YAML file. NOMB.
                log.warn("The following apikit:flows do not match any RAML API binding: " + apisInMuleConfigs);
                return yamlEntries;
            } else {
                Set<ResourceActionPair> flowEntries1 = new APIDiff(yamlEntries, muleFlowEntries).getEntries();
                log.info("Adding new apikit:flows to existing files for the following operations: " + flowEntries1);
                return flowEntries1;

            }
        }
    }
}
