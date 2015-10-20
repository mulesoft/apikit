/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.deployer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

public class MuleDeployPropertiesParser {

    public static final String PROPERTY_ENCODING = "encoding";
    public static final String PROPERTY_CONFIG_BUILDER = "config.builder";
    public static final String PROPERTY_DOMAIN = "domain";
    public static final String PROPERTY_CONFIG_RESOURCES = "config.resources";
    public static final String PROPERTY_REDEPLOYMENT_ENABLED = "redeployment.enabled";
    public static final String PROPERTY_LOADER_OVERRIDE = "loader.override";
    public static final String PROPERTY_SCAN_PACKAGES = "scan.packages";

    public MuleDeployProperties parse(File muleDeployFile) {
        try {
            final Properties p = loadProperties(new FileInputStream(muleDeployFile));

            MuleDeployProperties deployProperties = new MuleDeployProperties();
            deployProperties.setEncoding(p.getProperty(PROPERTY_ENCODING));
            deployProperties.setConfigurationBuilder(p.getProperty(PROPERTY_CONFIG_BUILDER));
            deployProperties.setDomain(p.getProperty(PROPERTY_DOMAIN));
            deployProperties.setPackagesToScan(p.getProperty(PROPERTY_SCAN_PACKAGES));

            final String resProps = p.getProperty(PROPERTY_CONFIG_RESOURCES);

            if (!StringUtils.isBlank(resProps)) {
                String[] urls;
                urls = resProps.split(",");
                deployProperties.setConfigResources(Arrays.asList(urls));
            }

            deployProperties.setRedeploymentEnabled(BooleanUtils.toBoolean(p.getProperty(PROPERTY_REDEPLOYMENT_ENABLED, Boolean.TRUE.toString())));
            deployProperties.setLoaderOverride(p.getProperty(PROPERTY_LOADER_OVERRIDE));
            return deployProperties;

        } catch(IOException ioe) {
            // Return default values
            return new MuleDeployProperties();
        }
    }


    private Properties loadProperties(FileInputStream is) throws IOException {
        if (is == null) {
            throw new IOException("Error, properties input stream is null");
        }

        try {
            Properties props = new Properties();
            props.load(is);
            return props;
        }

        finally {
            is.close();
        }
    }

}
