/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sun.jmx.mbeanserver.Util.cast;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.mule.apikit.common.CollectionUtils.join;
import static org.mule.runtime.api.deployment.meta.Product.MULE;
import static org.mule.tools.apikit.misc.FileListUtils.listFiles;

public class MuleArtifactJsonGenerator {

  private final File rootDirectory;
  private final String artifactName;
  private final String minMuleVersion;
  private final List<String> configFiles = ImmutableList.of("mule-config.xml");
  private final Log log;

  private static final String MULE_ARTIFACT_LOADER_DESCRIPTOR_ID = "mule";
  private static final String RESOURCES_PATH = "src/main/resources";
  private static final String API_RESOURCES_PATH = RESOURCES_PATH + "/api";
  private static final String MULE_ARTIFACT_FILENAME = "mule-artifact.json";

  private static final String EXPORTED_RESOURCES = "exportedResources";
  private static final String EXPORTED_PACKAGES = "exportedPackages";

  private final MuleApplicationModelJsonSerializer serializer = new MuleApplicationModelJsonSerializer();

  public MuleArtifactJsonGenerator(Log log, File rootDirectory, String minMuleVersion) {
    this(log, rootDirectory, rootDirectory.getName(), minMuleVersion);
  }

  public MuleArtifactJsonGenerator(Log log, File rootDirectory, String artifactName, String minMuleVersion) {
    this.log = log;
    this.rootDirectory = rootDirectory;
    this.artifactName = artifactName;
    this.minMuleVersion = minMuleVersion;
  }

  public void generate() {
    try {
      final MuleApplicationModel muleArtifactJson = generateDescriptor();
      saveDescriptor(muleArtifactJson);
    } catch (Exception e) {
      log.error("Error generating descriptor mule-artifact.json", e);
    }
  }

  private void saveDescriptor(MuleApplicationModel muleArtifactJson) throws IOException {
    final String asString = serializer.serialize(muleArtifactJson);
    Files.write(Paths.get(rootDirectory.getPath(), MULE_ARTIFACT_FILENAME), asString.getBytes());
  }

  MuleApplicationModel generateDescriptor() {
    try {
      final String json = IOUtils.toString(new FileInputStream(new File(rootDirectory, MULE_ARTIFACT_FILENAME)));

      final MuleApplicationModel muleArtifactJson = serializer.deserialize(json);

      final List<String> oldExportedResources =
          cast(muleArtifactJson.getClassLoaderModelLoaderDescriptor().getAttributes().get(EXPORTED_RESOURCES));
      final ImmutableList<String> newExportedResources = join(oldExportedResources, getApiResourcesList());

      return updateExportedResources(muleArtifactJson, newExportedResources);
    } catch (Exception e) {
      // Some Error has occurred
      return createDefaultMuleArtifactJson();
    }
  }

  private MuleApplicationModel createDefaultMuleArtifactJson() {
    final MuleApplicationModel.MuleApplicationModelBuilder builder = getMuleApplicationModelBuilder();
    builder.setName(artifactName);
    builder.setMinMuleVersion(minMuleVersion);
    builder.setConfigs(configFiles);
    builder.setRedeploymentEnabled(true);
    builder.setRequiredProduct(MULE);
    builder.withClassLoaderModelDescriptorLoader(
                                                 new MuleArtifactLoaderDescriptor(MULE_ARTIFACT_LOADER_DESCRIPTOR_ID,
                                                                                  ImmutableMap.of(EXPORTED_RESOURCES,
                                                                                                  getApiResourcesList(),
                                                                                                  EXPORTED_PACKAGES,
                                                                                                  emptyList())));
    builder.withBundleDescriptorLoader(new MuleArtifactLoaderDescriptor(MULE_ARTIFACT_LOADER_DESCRIPTOR_ID, emptyMap()));
    return builder.build();
  }

  private MuleApplicationModel updateExportedResources(MuleApplicationModel muleArtifactJson, List<String> exportedResources) {
    final MuleApplicationModel.MuleApplicationModelBuilder builder = getMuleApplicationModelBuilder(muleArtifactJson);

    final MuleArtifactLoaderDescriptor classLoaderModelLoaderDescriptor = muleArtifactJson.getClassLoaderModelLoaderDescriptor();

    Map<String, Object> attributes = classLoaderModelLoaderDescriptor.getAttributes().entrySet().stream()
        .collect(Collectors.toMap(
                                  Map.Entry::getKey,
                                  e -> EXPORTED_RESOURCES.equals(e.getKey()) ? exportedResources : e.getValue()));

    builder.withClassLoaderModelDescriptorLoader(new MuleArtifactLoaderDescriptor(classLoaderModelLoaderDescriptor.getId(),
                                                                                  attributes));

    return builder.build();
  }

  private MuleApplicationModel.MuleApplicationModelBuilder getMuleApplicationModelBuilder() {
    return new MuleApplicationModel.MuleApplicationModelBuilder();
  }

  private MuleApplicationModel.MuleApplicationModelBuilder getMuleApplicationModelBuilder(MuleApplicationModel muleArtifactJson) {
    final MuleApplicationModel.MuleApplicationModelBuilder builder = getMuleApplicationModelBuilder();
    builder.setName(muleArtifactJson.getName());
    builder.setMinMuleVersion(muleArtifactJson.getMinMuleVersion());
    builder.setConfigs(muleArtifactJson.getConfigs());
    builder.setRedeploymentEnabled(muleArtifactJson.isRedeploymentEnabled());
    builder.setRequiredProduct(muleArtifactJson.getRequiredProduct());
    builder.withBundleDescriptorLoader(muleArtifactJson.getBundleDescriptorLoader());
    builder.withClassLoaderModelDescriptorLoader(muleArtifactJson.getClassLoaderModelLoaderDescriptor());
    return builder;
  }

  private List<String> getApiResourcesList() {
    final Path rootPath = new File(rootDirectory, RESOURCES_PATH).toPath();
    return listFiles(rootDirectory.getPath(), API_RESOURCES_PATH).stream()
        .map(p -> rootPath.relativize(p).toString())
        .collect(Collectors.toList());
  }
}
