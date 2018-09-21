/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderReport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.mule.tools.apikit.model.RuntimeEdition;

import static org.mule.tools.apikit.Scaffolder.DEFAULT_MULE_VERSION;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_RUNTIME_EDITION;

public class ScaffolderAPI {

  private final static List<String> apiExtensions = Arrays.asList(".yaml", ".raml", ".yml");
  private final static List<String> appExtensions = Arrays.asList(".xml");

  public ScaffolderAPI() {

  }

  /**
   * Modifies or creates the Mule config files which are contained in the appDir directory
   * by running the scaffolder on the ramlFiles passed as parameter.
   *
   * @param ramlFiles the ramlFiles to which the scaffolder will be run on
   * @param appDir the directory which contained the generated Mule config files
   */

  public ScaffolderReport run(List<File> ramlFiles, File appDir) {
    return run(ramlFiles, appDir, null, DEFAULT_MULE_VERSION, DEFAULT_RUNTIME_EDITION);
  }

  /**
   * Modifies or creates the Mule config files which are contained in the appDir directory
   * by running the scaffolder on the ramlFiles passed as parameter.
   * Looks for an extension point and executes it, relying on the execute method otherwise.
   *
   * @param ramlFiles the ramlFiles to which the scaffolder will be run on
   * @param appDir the directory which contained the generated Mule config files
   * @param domainDir the directory which contained the domain used by the mule config files
   */
  public ScaffolderReport run(List<File> ramlFiles, File appDir, File domainDir) {
    return run(ramlFiles, appDir, domainDir, DEFAULT_MULE_VERSION, DEFAULT_RUNTIME_EDITION);
  }

  /**
   * Modifies or creates the Mule config files which are contained in the appDir directory
   * by running the scaffolder on the ramlFiles passed as parameter.
   *
   * @param ramlFiles the ramlFiles to which the scaffolder will be run on
   * @param appDir the directory which contained the generated Mule config files
   * @param domainDir the directory which contained the domain used by the mule config files
   * @param minMuleVersion currently unused, will be useful in future improvements
   * @param runtimeEdition the Mule Runtime Edition, this will be used to decide if generate CE or EE code
   */
  public ScaffolderReport run(List<File> ramlFiles, File appDir, File domainDir, String minMuleVersion,
                              RuntimeEdition runtimeEdition) {
    return execute(ramlFiles, appDir, domainDir, minMuleVersion, runtimeEdition);
  }

  private ScaffolderReport execute(List<File> ramlFiles, File appDir, File domainDir, String minMuleVersion,
                                   RuntimeEdition runtimeEdition) {
    List<String> ramlFilePaths = retrieveFilePaths(ramlFiles, apiExtensions);
    List<String> muleXmlFiles = retrieveFilePaths(appDir, appExtensions);
    SystemStreamLog log = new SystemStreamLog();
    String domain = null;
    if (domainDir != null) {
      List<String> domainFiles = retrieveFilePaths(domainDir, appExtensions);
      if (domainFiles.size() > 0) {
        domain = domainFiles.get(0);
        if (domainFiles.size() > 1) {
          log.info("There is more than one domain file inside of the domain folder. The domain: " + domain + " will be used.");
        }
      }
    }
    Scaffolder scaffolder;
    try {
      scaffolder = Scaffolder.createScaffolder(log, appDir, ramlFilePaths, muleXmlFiles, domain, minMuleVersion, runtimeEdition);
    } catch (Exception e) {
      throw new RuntimeException("Error executing scaffolder", e);
    }
    scaffolder.run();
    return scaffolder.getScaffolderReport();
  }

  private List<String> retrieveFilePaths(File dir, final List<String> extensions) {
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException("File " + dir.getName() + " must be a directory");
    }
    return retrieveFilePaths(new ArrayList<File>(Arrays.asList(dir.listFiles())), extensions);
  }

  private List<String> retrieveFilePaths(List<File> files, List<String> extensions) {
    List<String> filePaths = new ArrayList<String>();
    if (files != null) {
      for (File file : files) {
        if (containsValidExtension(file, extensions)) {
          filePaths.add(file.getAbsolutePath());
        }
      }
    }
    return filePaths;
  }

  private boolean containsValidExtension(File file, List<String> extensions) {
    for (String extension : extensions) {
      if (file.getName().toLowerCase().endsWith(extension)) {
        return true;
      }
    }
    return false;
  }
}
