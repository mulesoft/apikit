/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mule.maven.client.api.model.MavenConfiguration.newMavenConfigurationBuilder;

import org.mule.maven.client.api.model.MavenConfiguration;
import org.mule.maven.client.internal.DefaultLocalRepositorySupplierFactory;
import org.mule.maven.client.internal.DefaultSettingsSupplierFactory;
import org.mule.maven.client.internal.MavenEnvironmentVariables;
import org.mule.runtime.api.component.location.Location;
import org.mule.tooling.client.api.ToolingRuntimeClient;
import org.mule.tooling.client.api.artifact.ToolingArtifact;
import org.mule.tooling.client.api.datasense.DataSenseRequest;
import org.mule.tooling.client.api.descriptors.ArtifactDescriptor;
import org.mule.tooling.client.api.extension.ExtensionModelService;
import org.mule.tooling.client.bootstrap.ToolingRuntimeClientBootstrap;

import java.io.File;

import org.junit.Test;

public class ToolingClientTestCase
{
    @Test
    public void loadExtensionModel()
    {
        ToolingRuntimeClientBootstrap toolingRuntimeClientBootstrap = new ToolingRuntimeClientBootstrap("4.0.0-BETA.1-SNAPSHOT", createMavenConfiguration());
        ExtensionModelService extensionModelService = toolingRuntimeClientBootstrap.newToolingRuntimeClientBuilder().build().extensionModelService();
        ArtifactDescriptor artifactDescriptor = ArtifactDescriptor.newBuilder().withGroupId("org.mule.modules").withArtifactId("mule-apikit-module").withClassifier("mule-plugin").withVersion("1.0.0-BETA-SNAPSHOT").build();
        assertEquals("APIKit",extensionModelService.loadExtensionModel(artifactDescriptor).get().getName());
    }

    @Test
    public void loadDataSense()
    {
        ToolingRuntimeClientBootstrap toolingRuntimeClientBootstrap = new ToolingRuntimeClientBootstrap("4.0.0-BETA.1-SNAPSHOT", createMavenConfiguration());
        ToolingRuntimeClient toolingRuntimeClient = toolingRuntimeClientBootstrap.newToolingRuntimeClientBuilder().build();
        ToolingArtifact toolingArtifact = toolingRuntimeClient.newToolingArtifact(() -> this.getClass().getClassLoader().getResource("app"));
        assertNotNull(toolingArtifact);
        DataSenseRequest dataSenseRequest = new DataSenseRequest();
        dataSenseRequest.setLocation(Location.builder().globalName("doorway").addProcessorsPart().addIndexPart(1).build());
        dataSenseRequest.setMetadataTimeout(100);

        toolingArtifact.dataSenseService().resolveDataSense(dataSenseRequest);
    }

    public MavenConfiguration createMavenConfiguration()
    {
        MavenConfiguration.MavenConfigurationBuilder mavenConfigurationBuilder =
                newMavenConfigurationBuilder().withForcePolicyUpdateNever(true);

        final File localMavenRepository = new DefaultLocalRepositorySupplierFactory().environmentMavenRepositorySupplier().get();
        mavenConfigurationBuilder.withLocalMavenRepositoryLocation(localMavenRepository);

        final DefaultSettingsSupplierFactory settingsSupplierFactory =
                new DefaultSettingsSupplierFactory(new MavenEnvironmentVariables());

        settingsSupplierFactory.environmentUserSettingsSupplier().ifPresent(mavenConfigurationBuilder::withUserSettingsLocation);
        settingsSupplierFactory.environmentGlobalSettingsSupplier().ifPresent(mavenConfigurationBuilder::withGlobalSettingsLocation);

        return mavenConfigurationBuilder.build();
    }
}
