/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import org.mule.parser.service.ScaffoldingErrorType;
import org.mule.parser.service.SimpleScaffoldingError;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.mule.tools.apikit.model.RuntimeEdition.CE;
import static org.mule.tools.apikit.model.Status.*;

public class Scaffolder {

  public final static String DEFAULT_MULE_VERSION = "4.0.0";
  public final static RuntimeEdition DEFAULT_RUNTIME_EDITION = CE;

  private final MuleConfigGenerator muleConfigGenerator;
  private final MuleArtifactJsonGenerator muleArtifactJsonGenerator;
  private final ScaffolderReport.Builder scaffolderReportBuilder;



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
    Map<File, InputStream> apiStreams = fileUtils.toStreamsOrFail(specPaths);
    Map<File, InputStream> muleStreams = fileUtils.toStreamsOrFail(muleXmlPaths);
    Set<File> ramlWithExtensionEnabled = new TreeSet<>();

    if (ramlsWithExtensionEnabledPaths != null)
      log.warn("ExtensionEnabled is deprecated in mule 4");

    InputStream domainStream = getDomainStream(log, domainPath);
    return new Scaffolder(log, muleXmlOutputDirectory, apiStreams, muleStreams, domainStream, ramlWithExtensionEnabled,
                          minMuleVersion, runtimeEdition);
  }

  public Scaffolder(Log log, File muleXmlOutputDirectory, Map<File, InputStream> apis, Map<File, InputStream> xmls,
                    InputStream domainStream, Set<File> ramlsWithExtensionEnabled, String minMuleVersion,
                    RuntimeEdition runtimeEdition) {
    MuleDomainParser muleDomainParser = new MuleDomainParser(log, domainStream);
    APIFactory apiFactory = new APIFactory(muleDomainParser.getHttpListenerConfigs());
    MuleConfigParser muleConfigParser = new MuleConfigParser(log, apiFactory).parse(getFilePathSet(apis.keySet()), xmls);
    RAMLFilesParser filesParser = RAMLFilesParser.create(log, apis, apiFactory);
    List<GenerationModel> generationModels = new GenerationStrategy(log).generate(filesParser, muleConfigParser);

    scaffolderReportBuilder =
        new ScaffolderReport.Builder().withStatus(filesParser.getParseStatus()).withVersion(filesParser.getRamlVersion())
            .withVendorId(filesParser.getVendorId()).withScaffoldingErrors(filesParser.getParsingErrors());

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

  private Set<String> getFilePathSet(Set<File> fileSet) {
    Set<String> fileNamesSet = new HashSet<>();

    for (File file : fileSet) {
      fileNamesSet.add(file.getAbsolutePath());
    }

    return fileNamesSet;
  }

  public Scaffolder(Log log, File muleXmlOutputDirectory, Map<String, InputStream> apis,
                    ScaffolderResourceLoader scaffolderResourceLoader, Map<File, InputStream> xmls, InputStream domainStream,
                    String minMuleVersion, RuntimeEdition runtimeEdition) {
    MuleDomainParser muleDomainParser = new MuleDomainParser(log, domainStream);
    APIFactory apiFactory = new APIFactory(muleDomainParser.getHttpListenerConfigs());
    MuleConfigParser muleConfigParser = new MuleConfigParser(log, apiFactory).parse(apis.keySet(), xmls);
    RAMLFilesParser filesParser = RAMLFilesParser.create(log, apis, apiFactory, scaffolderResourceLoader);
    List<GenerationModel> generationModels = new GenerationStrategy(log).generate(filesParser, muleConfigParser);

    scaffolderReportBuilder =
        new ScaffolderReport.Builder().withStatus(filesParser.getParseStatus()).withVersion(filesParser.getRamlVersion())
            .withVendorId(filesParser.getVendorId()).withScaffoldingErrors(filesParser.getParsingErrors());

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

  private static InputStream getDomainStream(Log log, String domainPath) {
    InputStream domainStream = null;
    if (domainPath != null) {
      File domain = null;
      try {
        domain = new File(domainPath);
        domainStream = new FileInputStream(domain);
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

  public static Scaffolder createScaffolder(SystemStreamLog log, File appDir, Map<String, InputStream> apiSpecs,
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
    return scaffolderReportBuilder.build();
  }

  //TODO This is only a hack to get project base directory. Project Base Dir should be informed by api parameter
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
      scaffolderReportBuilder.withStatus(FAILED);
    } finally {
      scaffolderReportBuilder
          .withScaffoldingErrors(muleConfigGenerator.getErrors());
      scaffolderReportBuilder.withScaffoldingErrors(muleArtifactJsonGenerator.getErrors());
    }
  }
}
