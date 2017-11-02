/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.mule.apikit.common.CollectionUtils.join;
import static org.mule.apikit.common.CommonUtils.cast;
import static org.mule.runtime.api.deployment.meta.Product.MULE;
import static org.mule.tools.apikit.misc.FileListUtils.listFiles;

public class MuleArtifactJsonGenerator {

  private static final String API_PATH_PREFIX = "api/";
  private final File rootDirectory;
  private final String artifactName;
  private final String minMuleVersion;
  private final Set<String> configFiles = newHashSet("mule-config.xml");
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

  private MuleArtifactJsonGenerator(Log log, File rootDirectory, String artifactName, String minMuleVersion) {
    this.log = log;
    this.rootDirectory = rootDirectory;
    this.artifactName = artifactName;
    this.minMuleVersion = minMuleVersion;
  }

  public void generate() {
    try {
      save(generateArtifact());
    } catch (Exception e) {
      log.error("Error generating descriptor mule-artifact.json", e);
    }
  }

  private void save(MuleApplicationModel artifact) throws IOException {
    final String asString = serializer.serialize(artifact);
    Files.write(Paths.get(rootDirectory.getPath(), MULE_ARTIFACT_FILENAME), asString.getBytes());
  }

  MuleApplicationModel generateArtifact() {
    try {
      final String json = IOUtils.toString(new FileInputStream(new File(rootDirectory, MULE_ARTIFACT_FILENAME)));

      final MuleApplicationModel artifact = serializer.deserialize(json);

      final Collection<String> exportedResources = collectExportedResources(artifact);

      return updateExportedResources(artifact, exportedResources);
    } catch (Exception e) {
      log.warn("Error generating descriptor mule-artifact.json. Creating default descriptor.", e);
      return createDefaultArtifact();
    }
  }

  private Collection<String> collectExportedResources(MuleApplicationModel artifact) {
    final List<String> currentResources =
        cast(artifact.getClassLoaderModelLoaderDescriptor().getAttributes().get(EXPORTED_RESOURCES));

    final List<String> applicationResources =
        currentResources.stream().filter(resource -> !resource.startsWith(API_PATH_PREFIX)).collect(toList());
    return join(getApiResourcesList(), applicationResources);
  }

  private MuleApplicationModel createDefaultArtifact() {
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

  private MuleApplicationModel updateExportedResources(MuleApplicationModel artifact, Collection<String> exportedResources) {
    final MuleApplicationModel.MuleApplicationModelBuilder builder = getMuleApplicationModelBuilder(artifact);

    final MuleArtifactLoaderDescriptor descriptor = artifact.getClassLoaderModelLoaderDescriptor();

    final HashMap<String, Object> attributes = new HashMap<>(descriptor.getAttributes());
    attributes.put(EXPORTED_RESOURCES, exportedResources);

    builder.withClassLoaderModelDescriptorLoader(new MuleArtifactLoaderDescriptor(descriptor.getId(), attributes));

    return builder.build();
  }

  private MuleApplicationModel.MuleApplicationModelBuilder getMuleApplicationModelBuilder() {
    return new MuleApplicationModel.MuleApplicationModelBuilder();
  }

  private MuleApplicationModel.MuleApplicationModelBuilder getMuleApplicationModelBuilder(MuleApplicationModel artifact) {
    final MuleApplicationModel.MuleApplicationModelBuilder builder = getMuleApplicationModelBuilder();
    builder.setName(artifact.getName());
    builder.setMinMuleVersion(artifact.getMinMuleVersion());
    builder.setConfigs(artifact.getConfigs());
    builder.setRedeploymentEnabled(artifact.isRedeploymentEnabled());
    builder.setRequiredProduct(artifact.getRequiredProduct());
    builder.withBundleDescriptorLoader(artifact.getBundleDescriptorLoader());
    builder.withClassLoaderModelDescriptorLoader(artifact.getClassLoaderModelLoaderDescriptor());
    return builder;
  }

  private List<String> getApiResourcesList() {
    final Path rootPath = new File(rootDirectory, RESOURCES_PATH).toPath();
    return listFiles(rootDirectory.getPath(), API_RESOURCES_PATH).stream()
        .map(p -> rootPath.relativize(p).toString()).sorted()
        .collect(toList());
  }
}
