/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static java.util.Arrays.asList;
import static org.mule.module.apikit.api.RamlHandler.MULE_APIKIT_PARSER_AMF;

/**
 * Use this class when Test are not FunctionTests
 */
@RunWith(Parameterized.class)
public abstract class AbstractMultiParserTestCase {

  @Parameterized.Parameter(value = 0)
  public String parser;

  private static final String AMF_PARSER = "AmfParser";
  private static final String JAVA_PARSER = "JavaParser";

  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][] {
        {AMF_PARSER},
        {JAVA_PARSER}
    });
  }

  @Before
  public void beforeTest() {
    final Boolean value = AMF_PARSER.equals(parser);
    System.setProperty(MULE_APIKIT_PARSER_AMF, value.toString());
  }

  @After
  public static void afterTest() {
    System.clearProperty(MULE_APIKIT_PARSER_AMF);
  }
}
