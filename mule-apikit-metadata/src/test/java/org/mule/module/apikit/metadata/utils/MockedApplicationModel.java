/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.mule.runtime.config.api.XmlConfigurationDocumentLoader.noValidationDocumentLoader;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.processor.ArtifactConfig;
import org.mule.runtime.config.api.dsl.processor.ConfigFile;
import org.mule.runtime.config.api.dsl.processor.ConfigLine;
import org.mule.runtime.config.api.dsl.processor.xml.XmlApplicationParser;
import org.mule.runtime.config.internal.model.ApplicationModel;
import org.mule.runtime.config.internal.model.ComponentModel;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.registry.ServiceRegistry;
import org.mule.runtime.core.api.registry.SpiServiceRegistry;
import org.mule.runtime.dsl.api.component.ComponentBuildingDefinitionProvider;
import org.mule.runtime.module.extension.internal.config.ExtensionBuildingDefinitionProvider;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

public class MockedApplicationModel implements ApplicationModelWrapper {

  private final String name;
  private ApplicationModel applicationModel;
  private String typesData;

  private MockedApplicationModel(String name,
                                 ApplicationModel applicationModel,
                                 String typesData) {
    this.name = name;
    this.applicationModel = applicationModel;
    this.typesData = typesData;
  }

  public String getName() {
    return name;
  }

  public ApplicationModel getApplicationModel() {
    return applicationModel;
  }

  public static ApplicationModelWrapper load(String name, String content) throws Exception {
    return load(name, content, null);
  }

  public static ApplicationModelWrapper load(String name, String content, String typesData) throws Exception {
    Builder builder = new Builder();
    builder.addConfig(name, IOUtils.toInputStream(content));
    if (typesData != null) {
      builder.typesData(typesData);
    }
    return builder.build();

  }

  public static ApplicationModelWrapper load(String name, File appDir) throws Exception {
    return load(name, appDir, null, null);
  }

  public static ApplicationModelWrapper load(String name, File appDir, MuleContext muleContext) throws Exception {
    return load(name, appDir, null, muleContext);
  }

  public static ApplicationModelWrapper load(String name, File appDir, File typesDataFile) throws Exception {
    return load(name, appDir, typesDataFile, null);
  }

  public static ApplicationModelWrapper load(String name, File appDir, File typesDataFile, MuleContext muleContext)
      throws Exception {
    Builder builder = new Builder();
    builder.addConfig(name, new File(appDir, name));
    if (typesDataFile != null) {
      builder.typesData(typesDataFile);
    }
    if (muleContext != null) {
      builder.muleContext(muleContext);
    }
    return builder.build();
  }

  @Override
  public ComponentModel findRootComponentModel() {
    return getApplicationModel().getRootComponentModel();
  }

  @Override
  public Optional<ComponentModel> findNamedComponent(String name) {
    return getApplicationModel().findTopLevelNamedComponent(name);
  }

  @Override
  public Optional<String> findTypesData() {
    return Optional.ofNullable(typesData);
  }

  public static class Builder {

    private final SpiServiceRegistry serviceRegistry;
    private final XmlApplicationParser xmlApplicationParser;
    private final ArtifactConfig.Builder artifactConfigBuilder;
    private ResourceProvider resourceProvider;
    private MuleContext muleContext;
    private String typesData;

    public Builder() {
      artifactConfigBuilder = new ArtifactConfig.Builder();
      serviceRegistry = new SpiServiceRegistry();
      xmlApplicationParser = new XmlApplicationParser(serviceRegistry, emptyList());
    }

    public Builder muleContext(MuleContext muleContext) {
      Preconditions.checkNotNull(muleContext);
      this.muleContext = muleContext;
      return this;
    }

    public Builder resourceProvider(ResourceProvider resourceProvider) {
      Preconditions.checkNotNull(muleContext);
      this.resourceProvider = resourceProvider;
      return this;
    }

    public Builder typesData(String typesData) {
      Preconditions.checkNotNull(typesData);
      this.typesData = typesData;
      return this;
    }

    public Builder typesData(File typesDataFile) throws IOException {
      Preconditions.checkNotNull(typesDataFile);
      return typesData(IOUtils.toString(typesDataFile.toURI().toURL()));
    }

    public Builder addConfig(String configName, String configData) {
      Preconditions.checkNotNull(configName);
      Preconditions.checkNotNull(configData);
      return addConfig(configName, IOUtils.toInputStream(configData));
    }

    public Builder addConfig(String configName, File configData) throws IOException {
      Preconditions.checkNotNull(configName);
      Preconditions.checkNotNull(configData);
      try (FileInputStream fileInputStream = new FileInputStream(configData)) {
        return addConfig(configName, fileInputStream);
      }
    }

    public Builder addConfig(String configName, InputStream configData) {
      Preconditions.checkNotNull(configName);
      Preconditions.checkNotNull(configData);
      artifactConfigBuilder.addConfigFile(new ConfigFile(configName, Collections.singletonList(loadConfigLines(configData)
          .orElseThrow(() -> new IllegalArgumentException(String.format("Failed to parse %s.", configName))))));
      return this;
    }

    private Optional<ConfigLine> loadConfigLines(InputStream inputStream) {
      Document document = noValidationDocumentLoader().loadDocument("config", inputStream);
      return xmlApplicationParser.parse(document.getDocumentElement());
    }

    private static ComponentBuildingDefinitionRegistry createComponentBuildingDefinitionRegistry(
                                                                                                 Set<ExtensionModel> extensionModels,
                                                                                                 ClassLoader classLoader) {
      ServiceRegistry serviceRegistry = new SpiServiceRegistry();
      final ComponentBuildingDefinitionRegistry componentBuildingDefinitionRegistry =
          new ComponentBuildingDefinitionRegistry();
      serviceRegistry.lookupProviders(ComponentBuildingDefinitionProvider.class, classLoader)
          .forEach(componentBuildingDefinitionProvider -> {
            if (componentBuildingDefinitionProvider instanceof ExtensionBuildingDefinitionProvider) {
              ((ExtensionBuildingDefinitionProvider) componentBuildingDefinitionProvider)
                  .setExtensionModels(extensionModels);
            }
            componentBuildingDefinitionProvider.init();
            componentBuildingDefinitionProvider.getComponentBuildingDefinitions()
                .forEach(componentBuildingDefinitionRegistry::register);
          });
      return componentBuildingDefinitionRegistry;
    }

    private ResourceProvider getResourceProvider() {
      return Optional.ofNullable(resourceProvider).orElse(s -> {
        throw new UnsupportedOperationException();
      });
    }

    public MockedApplicationModel build() throws Exception {
      Set<ExtensionModel> extensionModels =
          Optional.ofNullable(muleContext).map(m -> m.getExtensionManager().getExtensions()).orElse(emptySet());

      final ComponentBuildingDefinitionRegistry componentBuildingDefinitionRegistry =
          createComponentBuildingDefinitionRegistry(extensionModels, muleContext != null ? muleContext.getClass().getClassLoader()
              : Thread.currentThread().getContextClassLoader());

      ApplicationModel applicationModel =
          new ApplicationModel(artifactConfigBuilder.build(), null,
                               extensionModels, Collections.emptyMap(),
                               Optional.empty(),
                               Optional.of(componentBuildingDefinitionRegistry),
                               false,
                               getResourceProvider());
      return new MockedApplicationModel("", applicationModel, typesData);
    }
  }
}
