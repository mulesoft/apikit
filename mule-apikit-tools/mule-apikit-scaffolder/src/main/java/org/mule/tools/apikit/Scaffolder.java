/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.mule.tools.apikit.model.RuntimeEdition.CE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.plugin.logging.Log;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.MuleDomainParser;
import org.mule.tools.apikit.input.RAMLFilesParser;
import org.mule.tools.apikit.misc.FileListUtils;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderReport;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.GenerationStrategy;
import org.mule.tools.apikit.output.MuleArtifactJsonGenerator;
import org.mule.tools.apikit.output.MuleConfigGenerator;

public class Scaffolder {

  public static final String MULE_DOMAIN_CONFIG_FILE_NAME = "mule-domain-config.xml";
  public final static String DEFAULT_MULE_VERSION = "4.0.0";
  public final static RuntimeEdition DEFAULT_RUNTIME_EDITION = CE;

  private final MuleConfigGenerator muleConfigGenerator;
  private final MuleArtifactJsonGenerator muleArtifactJsonGenerator;
  private final ScaffolderReport scaffolderReport;



  public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory, List<String> specFiles,
                                            List<String> muleXmlFiles)
      throws IOException {
    return createScaffolder(log, muleXmlOutputDirectory, specFiles, muleXmlFiles, null, DEFAULT_MULE_VERSION,
                            DEFAULT_RUNTIME_EDITION, null);
  }

  public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory, List<String> specFiles,
                                            List<String> muleXmlFiles, String domainFile)
      throws IOException {
    return createScaffolder(log, muleXmlOutputDirectory, specFiles, muleXmlFiles, domainFile, DEFAULT_MULE_VERSION,
                            DEFAULT_RUNTIME_EDITION, null);
  }

  public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory, List<String> specFiles,
                                            List<String> muleXmlFiles, String domainFile, String minMuleVersion,
                                            RuntimeEdition runtimeEdition)
      throws IOException {
    return createScaffolder(log, muleXmlOutputDirectory, specFiles, muleXmlFiles, domainFile, minMuleVersion, runtimeEdition,
                            null);
  }

  @Deprecated
  public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory, List<String> specPaths,
                                            List<String> muleXmlPaths, String domainPath, String minMuleVersion,
                                            RuntimeEdition runtimeEdition, List<String> ramlsWithExtensionEnabledPaths)
      throws IOException {
    FileListUtils fileUtils = new FileListUtils(log);
    Map<File, InputStream> muleStreams = fileUtils.toStreamsOrFail(muleXmlPaths);
    Set<File> ramlWithExtensionEnabled = new TreeSet<>();

    if (ramlsWithExtensionEnabledPaths != null)
      log.warn("ExtensionEnabled is deprecated in mule 4");

    InputStream domainStream = getDomainStream(log, domainPath);
    return new Scaffolder(log, muleXmlOutputDirectory, specPaths, muleStreams, domainStream, ramlWithExtensionEnabled,
                          minMuleVersion, runtimeEdition);
  }

  public Scaffolder(Log log, File muleXmlOutputDirectory, List<String> apis, Map<File, InputStream> xmls,
                    InputStream domainStream, Set<File> ramlsWithExtensionEnabled, String minMuleVersion,
                    RuntimeEdition runtimeEdition) {
    MuleDomainParser muleDomainParser = new MuleDomainParser(log, domainStream);
    APIFactory apiFactory = new APIFactory(muleDomainParser.getHttpListenerConfigs());
    MuleConfigParser muleConfigParser = new MuleConfigParser(log, apiFactory).parse(apis, xmls);
    RAMLFilesParser filesParser = RAMLFilesParser.create(log, apis, apiFactory);
    List<GenerationModel> generationModels = new GenerationStrategy(log).generate(filesParser, muleConfigParser);

    scaffolderReport = new ScaffolderReport();
    scaffolderReport.setVendorId(filesParser.getVendorId());
    scaffolderReport.setVersion(filesParser.getRamlVersion());
    scaffolderReport.setStatus(filesParser.getParseStatus());



    if (runtimeEdition == null) {
      runtimeEdition = DEFAULT_RUNTIME_EDITION;
    }

    if (minMuleVersion == null) {
      minMuleVersion = DEFAULT_MULE_VERSION;
    }

    muleConfigGenerator =
        new MuleConfigGenerator(log, muleXmlOutputDirectory, emptyList(), generationModels,
                                ramlsWithExtensionEnabled, minMuleVersion, runtimeEdition);

    muleArtifactJsonGenerator =
        new MuleArtifactJsonGenerator(log, getProjectBaseDirectory(muleXmlOutputDirectory), minMuleVersion);
  }

  public Scaffolder(Log log, File muleXmlOutputDirectory, List<String> apis,
                    ScaffolderResourceLoader scaffolderResourceLoader, Map<File, InputStream> xmls, InputStream domainStream,
                    String minMuleVersion, RuntimeEdition runtimeEdition) {
    MuleDomainParser muleDomainParser = new MuleDomainParser(log, domainStream);
    APIFactory apiFactory = new APIFactory(muleDomainParser.getHttpListenerConfigs());
    MuleConfigParser muleConfigParser = new MuleConfigParser(log, apiFactory).parse(apis, xmls);
    RAMLFilesParser filesParser = RAMLFilesParser.create(log, apis, apiFactory, scaffolderResourceLoader);
    List<GenerationModel> generationModels = new GenerationStrategy(log).generate(filesParser, muleConfigParser);

    scaffolderReport = new ScaffolderReport();
    scaffolderReport.setVendorId(filesParser.getVendorId());
    scaffolderReport.setVersion(filesParser.getRamlVersion());
    scaffolderReport.setStatus(filesParser.getParseStatus());

    if (runtimeEdition == null) {
      runtimeEdition = DEFAULT_RUNTIME_EDITION;
    }

    if (minMuleVersion == null) {
      minMuleVersion = DEFAULT_MULE_VERSION;
    }

    muleConfigGenerator =
        new MuleConfigGenerator(log, muleXmlOutputDirectory, newArrayList(muleConfigParser.getIncludedApis()),
                                generationModels, null, minMuleVersion, runtimeEdition);

    muleArtifactJsonGenerator =
        new MuleArtifactJsonGenerator(log, getProjectBaseDirectory(muleXmlOutputDirectory), minMuleVersion);
  }

  private static InputStream getDomainStream(Log log, String domainFile) {
    InputStream domainStream = null;
    if (domainFile != null) {
      File domain = null;
      domain = new File(domainFile);
      try {
        if (isArtifactDomainFile(domainFile)) {
          try (URLClassLoader cl = new URLClassLoader(new URL[] {domain.toURI().toURL()},
                                                      Scaffolder.class.getClassLoader())) {
            domainStream = cloneInputStream(cl.getResourceAsStream(MULE_DOMAIN_CONFIG_FILE_NAME));
          } catch (IOException e) {
            log.error("There was an error reading the external domain configuration file.", new RuntimeException(e));
          }
        } else {
          domainStream = new FileInputStream(domain);
        }
      } catch (FileNotFoundException e) {
        if (log != null) {
          log.error("Error opening file [" + domain + "] file", e);
        } else {
          throw new RuntimeException(e);
        }
      }
    }
    return domainStream;
  }

  private static InputStream cloneInputStream(InputStream toClone) throws IOException {
    ByteArrayOutputStream middleMan = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len;
    while ((len = toClone.read(buffer)) > -1) {
      middleMan.write(buffer, 0, len);
    }
    return new ByteArrayInputStream(middleMan.toByteArray());
  }

  private static boolean isArtifactDomainFile(String domainFile) {
    return domainFile.endsWith(".jar");
  }

  public static Scaffolder createScaffolder(Log log, File appDir, List<String> apiSpecs,
                                            ScaffolderResourceLoader scaffolderResourceLoader, List<String> muleXmlFiles,
                                            String domain, String minMuleVersion, RuntimeEdition runtimeEdition)
      throws IOException {
    FileListUtils fileUtils = new FileListUtils(log);
    Map<File, InputStream> muleStreams = fileUtils.toStreamsOrFail(muleXmlFiles);
    InputStream domainStream = getDomainStream(log, domain);

    return new Scaffolder(log, appDir, apiSpecs, scaffolderResourceLoader, muleStreams, domainStream, minMuleVersion,
                          runtimeEdition);

  }

  public ScaffolderReport getScaffolderReport() {
    return scaffolderReport;
  }

  // TODO This is only a hack to get project base directory. Project Base Dir should be informed by api parameter
  private File getProjectBaseDirectory(File muleXmlOutputDirectory) {
    final Path outputDirectory = muleXmlOutputDirectory.toPath();

    if (outputDirectory.endsWith("src/main/mule") || outputDirectory.endsWith("src/main/mule/")) {
      return outputDirectory.getParent().getParent().getParent().toFile();
    } else {
      return muleXmlOutputDirectory;
    }
  }

  public void run() {
    try {
      muleConfigGenerator.generate();
      muleArtifactJsonGenerator.generate();
    } catch (Exception e) {
      scaffolderReport.setStatus(ScaffolderReport.FAILED);
    }

  }


}
