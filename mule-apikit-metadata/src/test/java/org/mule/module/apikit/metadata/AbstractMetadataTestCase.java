/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.internal.utils.MetadataTypeWriter;
import org.mule.module.apikit.metadata.api.Metadata;
import org.mule.module.apikit.metadata.internal.model.ApplicationModelWrapper;
import org.mule.module.apikit.metadata.internal.model.Flow;
import org.mule.module.apikit.metadata.utils.MockedApplicationModel;
import org.mule.module.apikit.metadata.utils.TestNotifier;
import org.mule.module.apikit.metadata.utils.TestResourceLoader;
import org.mule.runtime.config.internal.model.ApplicationModel;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class AbstractMetadataTestCase {

  protected static final String AMF = "AMF";
  protected static final String RAML = "RAML";

  private static final PathMatcher API_MATCHER = FileSystems.getDefault().getPathMatcher("glob:app.xml");

  protected static List<File> scanApps() throws IOException, URISyntaxException {
    final URI baseFolder = AbstractMetadataTestCase.class.getResource("").toURI();
    return scan(baseFolder);
  }

  protected static List<File> scan(final URI resources) throws IOException {

    return Files.walk(Paths.get(resources))
        //.peek(path -> System.out.println("Path:" + path + " isMuleApp:" + API_MATCHER.matches(path.getFileName())))
        .filter(path -> Files.isRegularFile(path) && API_MATCHER.matches(path.getFileName()))
        .map(Path::toFile)
        .collect(toList());
  }

  protected static ApplicationModel createApplicationModel(final File app) throws Exception {
    final MockedApplicationModel.Builder builder = new MockedApplicationModel.Builder();
    builder.addConfig("apiKitSample", app);
    final MockedApplicationModel mockedApplicationModel = builder.build();
    return mockedApplicationModel.getApplicationModel();
  }

  protected static List<Flow> findFlows(final File app) throws Exception {
    final ApplicationModel applicationModel = createApplicationModel(app);

    // Only flow with metadata included
    return ApplicationModelWrapper.findFlows(applicationModel).stream()
        .filter(flow -> hasMetadata(applicationModel, flow)).collect(toList());
  }

  private static boolean hasMetadata(final ApplicationModel applicationModel, final Flow flow) {
    try {
      return getMetadata(applicationModel, flow).isPresent();
    } catch (Exception e) {
      return false;
    }
  }

  protected static Optional<FunctionType> getMetadata(Metadata metadata, Flow flow) {
    return metadata.getMetadataForFlow(flow.getName());
  }

  protected static Optional<FunctionType> getMetadata(final ApplicationModel applicationModel, final Flow flow) throws Exception {

    final Metadata metadata = new Metadata.Builder()
        .withApplicationModel(applicationModel)
        .withResourceLoader(new TestResourceLoader())
        .withNotifier(new TestNotifier()).build();

    return metadata.getMetadataForFlow(flow.getName());
  }

  protected static String metadataToString(final FunctionType functionType) {
    return new MetadataTypeWriter().toString(functionType);
  }

  protected File goldenFile(final Flow flow, final File app, final String parser) {
    final String fileName = flow.getName()
        .replace("\\", "")
        .replace(":", "-") + ".out";

    final File parserFolder = new File(app.getParentFile(), parser.toLowerCase());
    return new File(parserFolder, fileName);
  }

  protected static String readFile(final Path path) {
    try {
      return new String(Files.readAllBytes(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static Path createGoldenFile(final File goldenFile, final String content) throws IOException {

    final String srcPath = goldenFile.getPath().replace("target/test-classes", "src/test/resources");
    final Path goldenPath = Paths.get(srcPath);
    System.out.println("*** Create Golden " + goldenPath);

    // Write golden files  with current values
    final Path parent = goldenPath.getParent();
    if (!Files.exists(parent))
      Files.createDirectory(parent);
    return Files.write(goldenPath, content.getBytes("UTF-8"));
  }
}
