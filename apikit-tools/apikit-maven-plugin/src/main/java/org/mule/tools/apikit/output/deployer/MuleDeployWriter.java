/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class MuleDeployWriter {

    private static final String MULE_CONFIG_PATTERN = "*.xml";
    private static final String DEPLOY_FILE_PATTERN = "mule-deploy.properties";
    private static final String DEFAULT_CONFIGURATION_RESOURCE = "mule-config.xml";

    private final File appDir;

    public MuleDeployWriter(final File appDir) {
        this.appDir = appDir;
    }

    public void generate() {
        try {
            Collection<File> muleFiles = FileUtils.listFiles(appDir, new WildcardFileFilter(MULE_CONFIG_PATTERN), null);
            List<String> configNames = getConfigNames(muleFiles);
            if(generateDeployProperties(configNames)) {
                File deployPropertiesFile = new File(appDir, DEPLOY_FILE_PATTERN);
                MuleDeployProperties deployProperties = updateConfigResources(configNames, deployPropertiesFile);
                MuleDeployPropertiesWriter.write(deployProperties, deployPropertiesFile);
            }
        } catch(Exception e) {
            // Do nothing
        }
    }

    private boolean generateDeployProperties(List<String> configNames) {
        return !configNames.isEmpty() &&  !(configNames.size() == 1 && DEFAULT_CONFIGURATION_RESOURCE.equals(configNames.get(0)));
    }

    private MuleDeployProperties updateConfigResources(List<String> configNames, File deployPropertiesFile) {
        MuleDeployProperties properties;
        if(deployPropertiesFile.exists()) {
            properties = new MuleDeployPropertiesParser().parse(deployPropertiesFile);
        } else {
            properties = new MuleDeployProperties();
        }

        Set<String> mergeConfigResources = new HashSet<String>(properties.getConfigResources());
        mergeConfigResources.addAll(configNames);

        List<String> allResources = new ArrayList<String>(mergeConfigResources);

        // If there is a muleConfig, add it first
        if(allResources.contains(DEFAULT_CONFIGURATION_RESOURCE)) {
            allResources.remove(DEFAULT_CONFIGURATION_RESOURCE);
            allResources.add(0, DEFAULT_CONFIGURATION_RESOURCE);
        }

        properties.setConfigResources(allResources);

        return properties;
    }

    private List<String> getConfigNames(Collection<File> muleFiles) {
        List<String> configNames = new ArrayList<String>();

        if(muleFiles != null && !muleFiles.isEmpty()) {
            for(File configFile : muleFiles) {
                configNames.add(configFile.getName());
            }
        }
        return configNames;
    }
}
