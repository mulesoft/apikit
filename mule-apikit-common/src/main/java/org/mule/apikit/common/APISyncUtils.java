/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.common;

public class APISyncUtils {

  public static final String EXCHANGE_JSON = "exchange.json";
  public static final String API_SYNC_PROTOCOL = "resource::";
  public static final String RESOURCE_FORMAT = "resource::%s:%s:%s:%s:%s:%s";
  public static final String RAML_FRAGMENT_CLASSIFIER = "raml-fragment";
  public static final String EXCHANGE_TYPE = "zip";
  public static final String EXCHANGE_MODULES = "exchange_modules";
  final static String EXCHANGE_ROOT_RAML_TAG = "\"main\":\"";


  private APISyncUtils() {}

  public static boolean isSyncProtocol(final String path) {
    return path.startsWith(API_SYNC_PROTOCOL);
  }

  public static String getFileName(final String apiSyncResource) {
    return apiSyncResource.substring(apiSyncResource.lastIndexOf(":") + 1);
  }

  public static boolean isExchangeModules(final String path) {
    return path.startsWith(EXCHANGE_MODULES) || path.startsWith("/" + EXCHANGE_MODULES);
  }

  public static String getMainRaml(String exchangeJson) {
    exchangeJson = exchangeJson.substring(exchangeJson.indexOf(EXCHANGE_ROOT_RAML_TAG) + EXCHANGE_ROOT_RAML_TAG.length());
    exchangeJson = exchangeJson.substring(0, exchangeJson.indexOf("\""));
    return exchangeJson;
  }

  public static String toApiSyncResource(String s) {
    String apiSyncResource = null;
    String[] resourceParts = s.split("/");
    int length = resourceParts.length;
    if (length > 4)
      apiSyncResource = String.format(RESOURCE_FORMAT, resourceParts[length - 4], resourceParts[length - 3],
                                      resourceParts[length - 2], RAML_FRAGMENT_CLASSIFIER,
                                      EXCHANGE_TYPE, resourceParts[length - 1]);
    return apiSyncResource;
  }

  public static boolean compareResources(String resourceA, String resourceB, Boolean checkVersion) {
    APISyncResource apiSyncResourceA = null;
    APISyncResource apiSyncResourceB = null;
    try {
      apiSyncResourceA = new APISyncResource(resourceA);
      apiSyncResourceB = new APISyncResource(resourceB);
    } catch (APISyncResourceException e) {
      return false;
    }

    return apiSyncResourceA.equals(apiSyncResourceB, checkVersion);
  }

  private static class APISyncResource {

    private String groupId;
    private String artifact;
    private String version;
    private String classifier;
    private String packager;
    private String file;


    APISyncResource(String resource) throws APISyncResourceException {
      if (!APISyncUtils.isSyncProtocol(resource))
        throw new APISyncResourceException("Invalid APISync Resource");

      String[] parts = resource.substring(APISyncUtils.API_SYNC_PROTOCOL.length()).split(":");

      if (parts.length != 6)
        throw new APISyncResourceException("Invalid APISync Resource");

      groupId = parts[0];
      artifact = parts[1];
      version = parts[2];
      classifier = parts[3];
      packager = parts[4];
      file = parts[5];
    }

    public boolean equals(APISyncResource resource, Boolean checkVersion) {
      if (checkVersion == false) {
        return groupId.equals(resource.groupId) && artifact.equals(resource.artifact) &&
            classifier.equals(resource.classifier) && packager.equals(resource.packager) && file.equals(resource.file);

      }

      return groupId.equals(resource.groupId) && artifact.equals(resource.artifact) && version.equals(resource.version)
          && classifier.equals(resource.classifier) && packager.equals(resource.packager) &&
          file.equals(resource.file);
    }

  }

  private static class APISyncResourceException extends Exception {

    public APISyncResourceException(String message) {
      super(message);
    }

  }
}
