/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MuleArtifactJsonGenerator {

  private final File rootDirectory;
  private final String minMuleVersion;
  private final Log log;

  private static final String MULE_ARTIFACT_FILENAME = "mule-artifact.json";
  private static final String MIN_MULE_VERSION = "minMuleVersion";

  public MuleArtifactJsonGenerator(Log log, File rootDirectory, String minMuleVersion) {
    this.log = log;
    this.rootDirectory = rootDirectory;
    this.minMuleVersion = minMuleVersion;
  }

  public void generate() {
    try {
      save(generateArtifact());
    } catch (Exception e) {
      log.error("Error generating descriptor mule-artifact.json", e);
    }
  }

  private void save(String artifactJson) throws IOException {
    Files.write(Paths.get(rootDirectory.getPath(), MULE_ARTIFACT_FILENAME), artifactJson.getBytes());
  }

  String generateArtifact() {
    try {
      final String json = IOUtils.toString(new FileInputStream(new File(rootDirectory, MULE_ARTIFACT_FILENAME)));
      JSONObject muleArtifact = new JSONObject(json);

      if (muleArtifact.keySet().contains(MIN_MULE_VERSION))
        return muleArtifact.toString();
    } catch (IOException | JSONException e) {
      e.printStackTrace();
    }
    return new JSONObject().put(MIN_MULE_VERSION, this.minMuleVersion).toString();
  }
}
