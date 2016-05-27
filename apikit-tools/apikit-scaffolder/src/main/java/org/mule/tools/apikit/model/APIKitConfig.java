/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.apache.commons.lang.StringUtils;

public class APIKitConfig {

    public static final String ELEMENT_NAME = "config";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String RAML_ATTRIBUTE = "raml";
    public static final String CONSOLE_ENABLED_ATTRIBUTE = "consoleEnabled";
    public static final String EXTENSION_ENABLED_ATTRIBUTE = "extensionEnabled";
    public static final String CONSOLE_PATH_ATTRIBUTE = "consolePath";
    public static final String DEFAULT_CONSOLE_PATH = "console";
    public static final String DEFAULT_CONFIG_NAME = "config";


    private String name;
    private String raml;
    private boolean consoleEnabled;
    private Boolean extensionEnabled;
    private String consolePath;

    public static class Builder {
        private String name;
        private final String raml;
        private boolean consoleEnabled = false;
        private Boolean extensionEnabled = null;
        private String consolePath;

        public Builder(final String raml) {
            if(StringUtils.isEmpty(raml)) {
                throw new IllegalArgumentException("Raml attribute cannot be null or empty");
            }
            this.raml = raml;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setConsoleEnabled(boolean consoleEnabled) {
            this.consoleEnabled = consoleEnabled;
            return this;
        }

        public Builder setExtensionEnabled(boolean extensionEnabled) {
            this.extensionEnabled = extensionEnabled;
            return this;
        }

        public Builder setConsolePath(String consolePath) {
            this.consolePath = consolePath;
            return this;
        }

        public APIKitConfig build() {
            return new APIKitConfig(this.name, this.raml, this.consoleEnabled, this.extensionEnabled, this.consolePath != null? this.consolePath : DEFAULT_CONSOLE_PATH);
        }
    }

    private APIKitConfig(final String name,
                         final String raml,
                         final boolean consoleEnabled,
                         final Boolean extensionEnabled,
                         final String consolePath) {
        this.name = name;
        this.raml = raml;
        this.consoleEnabled = consoleEnabled;
        this.extensionEnabled = extensionEnabled;
        this.consolePath = consolePath;
    }

    public String getName() {
        return name;
    }

    public String getRaml() {
        return raml;
    }

    public boolean isConsoleEnabled() {
        return consoleEnabled;
    }

    public Boolean isExtensionEnabled() {
        return extensionEnabled;
    }

    public void setExtensionEnabled(Boolean enabled)
    {
        this.extensionEnabled = enabled;
    }

    public String getConsolePath() {
        return consolePath;
    }

}
