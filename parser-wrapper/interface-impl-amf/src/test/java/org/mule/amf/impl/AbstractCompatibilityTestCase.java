/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.raml.implv1.ParserV1Utils;
import org.mule.raml.implv1.ParserWrapperV1;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.implv2.ParserWrapperV2;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.model.IRaml;
import org.raml.v2.api.loader.DefaultResourceLoader;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertNotNull;


abstract class AbstractCompatibilityTestCase extends AbstractTestCase {

  protected final IRaml amf;
  protected final IRaml raml;
  protected final boolean isRaml08;

  protected ParserWrapper ramlWrapper;
  protected ParserWrapper amfWrapper;

  private static final PathMatcher API_MATCHER = FileSystems.getDefault().getPathMatcher("glob:api.raml");

  AbstractCompatibilityTestCase(final File input, final String name) {
    final URI uri = input.toURI();
    final String apiPath = uri.toString();
    isRaml08 = isRaml08(input);
    // Create Java Parser Wrapper
    ramlWrapper = isRaml08 ? new ParserWrapperV1(apiPath) : new ParserWrapperV2(apiPath);
    raml = ramlWrapper.build();
    assertNotNull(raml);
    // Create AMF Wrapper
    amfWrapper = ParserWrapperAmf.create(uri);
    amf = amfWrapper.build();
    assertNotNull(amf);

  }

  static Collection<Object[]> getData(final URI baseFolder) throws IOException {
    final List<File> apis = scan(baseFolder);

    final List<Object[]> parameters = new ArrayList<>();
    apis.forEach(api -> parameters.add(new Object[] {api, api.getParentFile().getName()}));

    return parameters;
  }

  private static List<File> scan(final URI resources) throws IOException {

    return Files.walk(Paths.get(resources))
        // .peek(path -> System.out.println("Path:" + path + " isApi:" + API_MATCHER.matches(path.getFileName())))
        .filter(path -> Files.isRegularFile(path) && API_MATCHER.matches(path.getFileName()))
        .map(Path::toFile)
        .collect(toList());
  }

  private static boolean isRaml08(final File file) {
    boolean result = false;
    try {
      final Stream<String> lines = Files.lines(file.toPath());
      final Optional<String> first = lines.findFirst();
      if (first.isPresent())
        result = first.get().contains("#%RAML 0.8");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

}
