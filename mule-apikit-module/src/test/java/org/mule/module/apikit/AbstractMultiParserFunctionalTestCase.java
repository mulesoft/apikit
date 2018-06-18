/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;
import org.mule.test.runner.RunnerDelegateTo;

import static java.lang.Boolean.valueOf;
import static java.util.Arrays.asList;

@RunnerDelegateTo(Parameterized.class)
@ArtifactClassLoaderRunnerConfig
public abstract class AbstractMultiParserFunctionalTestCase extends MuleArtifactFunctionalTestCase {

  private static final String MULE_APIKIT_PARSER_AMF = "mule.apikit.parser.amf";

  @Parameterized.Parameter(value = 0)
  public String parser;

  private static String AMF_PARSER = "AmfParser";
  private static String JAVA_PARSER = "JavaParser";

  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][] {
        {JAVA_PARSER},
        {AMF_PARSER}
    });
  }

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  public int getTestTimeoutSecs() {
    return 6000;
  }

  @Override
  protected void doSetUp() throws Exception {
    RestAssured.port = serverPort.getNumber();
    super.doSetUp();
  }

  @Before
  public void beforeTest() {
    System.setProperty(MULE_APIKIT_PARSER_AMF, valueOf(isAmfParser()).toString());
  }

  protected boolean isAmfParser() {
    return AMF_PARSER.equals(parser);
  }
}
