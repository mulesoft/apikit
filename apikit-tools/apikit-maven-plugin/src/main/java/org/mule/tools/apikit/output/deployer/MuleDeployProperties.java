package org.mule.tools.apikit.output.deployer;

import java.util.ArrayList;
import java.util.List;

public class MuleDeployProperties {

    private String domain = "default";
    private List<String> configResources = new ArrayList<String>();
    private boolean redeploymentEnabled = true;
    private String encoding = "UTF-8";
    private String configurationBuilder;
    private String packagesToScan;
    private String loaderOverride;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getConfigResources() {
        return configResources;
    }

    public void setConfigResources(List<String> configResources) {
        this.configResources = configResources;
    }

    public boolean isRedeploymentEnabled() {
        return redeploymentEnabled;
    }

    public void setRedeploymentEnabled(boolean redeploymentEnabled) {
        this.redeploymentEnabled = redeploymentEnabled;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getConfigurationBuilder() {
        return configurationBuilder;
    }

    public void setConfigurationBuilder(String configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
    }

    public String getPackagesToScan() {
        return packagesToScan;
    }

    public void setPackagesToScan(String packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    public String getLoaderOverride() {
        return loaderOverride;
    }

    public void setLoaderOverride(String loaderOverride) {
        this.loaderOverride = loaderOverride;
    }
}
