/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model.api;

import org.mule.raml.interfaces.common.APISyncUtils;
import org.mule.raml.interfaces.loader.ApiSyncResourceLoader;
import org.mule.raml.interfaces.loader.ClassPathResourceLoader;
import org.mule.raml.interfaces.loader.ResourceLoader;

import java.io.InputStream;
import java.util.Optional;

import static java.lang.String.format;

class ApiSyncApiRef implements ApiRef {

  private static final String RESOURCE_FORMAT = "resource::%s:%s:%s:%s:%s:%s";

  private String groupId;
  private String artifact;
  private String version;
  private String classifier;
  private String packager;
  private String file;

  private ResourceLoader resourceLoader;

  ApiSyncApiRef(String resource) {
    this(resource, new ClassPathResourceLoader());
  }

  ApiSyncApiRef(String resource, ResourceLoader resourceLoader) {
    this.resourceLoader = new ApiSyncResourceLoader(resource, resourceLoader);
    if (!APISyncUtils.isSyncProtocol(resource))
      throw new RuntimeException("Invalid APISync Resource");

    String[] parts = resource.substring(APISyncUtils.API_SYNC_PROTOCOL.length()).split(":");

    if (parts.length != 6)
      throw new RuntimeException("Invalid APISync Resource");

    groupId = parts[0];
    artifact = parts[1];
    version = parts[2];
    classifier = parts[3];
    packager = parts[4];
    file = parts[5];
  }

  public boolean equals(ApiSyncApiRef resource, Boolean checkVersion) {

    return groupId.equals(resource.groupId)
        && artifact.equals(resource.artifact)
        && (!checkVersion || version.equals(resource.version))
        && classifier.equals(resource.classifier)
        && packager.equals(resource.packager)
        && file.equals(resource.file);
  }

  @Override
  public String getLocation() {
    return format(RESOURCE_FORMAT, groupId, artifact, version, classifier, packager, file);
  }

  @Override
  public String getFormat() {
    return classifier.toUpperCase();
  }

  @Override
  public InputStream resolve() {
    return resourceLoader.getResourceAsStream(getLocation());
  }

  @Override
  public Optional<ResourceLoader> getResourceLoader() {
    return Optional.of(resourceLoader);
  }
}
