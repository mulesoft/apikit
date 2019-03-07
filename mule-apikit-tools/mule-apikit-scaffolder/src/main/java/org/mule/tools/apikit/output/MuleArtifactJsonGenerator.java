/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.mule.parser.service.ComponentScaffoldingError;
import org.mule.parser.service.SimpleScaffoldingError;

public class MuleArtifactJsonGenerator {

  private final File rootDirectory;
  private final String minMuleVersion;
  private final Log log;
  private final List<ComponentScaffoldingError> errors = new LinkedList<>();

  private static final String MULE_ARTIFACT_FILENAME = "mule-artifact.json";
  private static final String MIN_MULE_VERSION = "minMuleVersion";

  public MuleArtifactJsonGenerator(Log log, File rootDirectory, String minMuleVersion) {
    this.log = log;
    this.rootDirectory = rootDirectory;
    this.minMuleVersion = minMuleVersion;
  }

  public List<ComponentScaffoldingError> getErrors() {
    return errors;
  }

  public void generate() {
    try {
      save(generateArtifact());
    } catch (Exception e) {
      log.error("Error generating descriptor mule-artifact.json", e);
      errors
          .add(new SimpleScaffoldingError(String.format("Error generating descriptor mule-artifact.json : %s", e.getMessage())));
    }
  }

  private void save(String artifactJson) throws IOException {
    Files.write(Paths.get(rootDirectory.getPath(), MULE_ARTIFACT_FILENAME), artifactJson.getBytes());
  }

  String generateArtifact() {
    try (FileInputStream input = new FileInputStream(new File(rootDirectory, MULE_ARTIFACT_FILENAME))) {
      final String json = IOUtils.toString(input);
      JSONObject muleArtifact = new JSONObject(json);

      if (muleArtifact.keySet().contains(MIN_MULE_VERSION))
        return muleArtifact.toString();
    } catch (IOException | JSONException e) {
      e.printStackTrace();
    }
    return new JSONObject().put(MIN_MULE_VERSION, this.minMuleVersion).toString();
  }
}
