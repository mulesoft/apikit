/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.raml.implv1.ParserV1Utils;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.raml.v2.api.loader.DefaultResourceLoader;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class CompatibilityRaml10TestCase extends AbstractCompatibilityTestCase {

  public CompatibilityRaml10TestCase(final String api) {
    super(api);
  }

  @Override
  IRaml buildRaml(final String api) {
    return buildRaml10(api);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][] {{"raml-10/sanity.raml"}, {"raml-10/leagues/input.raml"}});
  }

  private static IRaml buildRaml10(final String resource) {
    final URL url = ParserAmfUtils.class.getResource(resource);
    final File file = new File(url.getFile());

    try {
      final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
      return ParserV2Utils.build(resourceLoader, file.getAbsolutePath());
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    return null;
  }

}
