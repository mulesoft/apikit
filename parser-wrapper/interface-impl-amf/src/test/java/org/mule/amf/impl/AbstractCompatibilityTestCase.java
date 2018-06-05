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
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.raml.implv1.ParserV1Utils;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.interfaces.model.IRaml;
import org.raml.v2.api.loader.DefaultResourceLoader;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertNotNull;


abstract class AbstractCompatibilityTestCase extends AbstractTestCase {

  protected final IRaml amf;
  protected final IRaml raml;

  private static final PathMatcher API_MATCHER = FileSystems.getDefault().getPathMatcher("glob:api.raml");

  AbstractCompatibilityTestCase(final File input, final String name) {
    amf = ParserAmfUtils.build(input);
    assertNotNull(amf);
    raml = name.startsWith("08-") ? buildRaml08(input) : buildRaml10(input);
    assertNotNull(raml);
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

  private static IRaml buildRaml08(final File file) {
    try {
      final FileReader fileReader = new FileReader(file);
      final String content = IOUtils.toString(fileReader);
      fileReader.close();

      String rootRamlName = file.getName();
      String ramlFolderPath = null;
      if (file.getParentFile() != null) {
        ramlFolderPath = file.getParentFile().getPath();
      }
      return ParserV1Utils.build(content, ramlFolderPath, rootRamlName);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    return null;
  }

  private static IRaml buildRaml10(final File file) {

    try {
      final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
      return ParserV2Utils.build(resourceLoader, file.getAbsolutePath());
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    return null;
  }

}
