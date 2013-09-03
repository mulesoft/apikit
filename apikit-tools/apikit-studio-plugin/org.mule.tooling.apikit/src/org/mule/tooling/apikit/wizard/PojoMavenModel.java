package org.mule.tooling.apikit.wizard;

import org.mule.tooling.core.utils.IMavenModel;

public class PojoMavenModel implements IMavenModel {

    private String groupId;
    private String artifactId;
    private String version;
    private boolean createPom;

    public PojoMavenModel(boolean createPom, String groupId, String artifactId, String version) {
        super();
        this.createPom = createPom;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public void setCreatePom(boolean createPom) {
        this.createPom = createPom;
    }

    public boolean isCreatePom() {
        return createPom;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}