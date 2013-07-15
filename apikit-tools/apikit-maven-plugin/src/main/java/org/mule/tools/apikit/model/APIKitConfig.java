package org.mule.tools.apikit.model;

import org.apache.commons.lang.StringUtils;

public class APIKitConfig {

    public static final String ELEMENT_NAME = "config";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String RAML_ATTRIBUTE = "raml";
    public static final String CONSOLE_ENABLED_ATTRIBUTE = "consoleEnabled";
    public static final String CONSOLE_PATH_ATTRIBUTE = "consolePath";
    public static final String DEFAULT_CONSOLE_PATH = "console";

    private String name;
    private String raml;
    private boolean consoleEnabled;
    private String consolePath;

    public static class Builder {
        private String name;
        private final String raml;
        private boolean consoleEnabled = true;
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

        public Builder setConsolePath(String consolePath) {
            this.consolePath = consolePath;
            return this;
        }

        public APIKitConfig build() {
            return new APIKitConfig(this.name, this.raml, this.consoleEnabled, this.consolePath != null? this.consolePath : DEFAULT_CONSOLE_PATH);
        }
    }

    private APIKitConfig(final String name,
                         final String raml,
                         final boolean consoleEnabled,
                         final String consolePath) {
        this.name = name;
        this.raml = raml;
        this.consoleEnabled = consoleEnabled;
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

    public String getConsolePath() {
        return consolePath;
    }

}
