/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import static java.util.Arrays.asList;
import static org.mule.tools.apikit.input.RAMLFilesParser.MULE_APIKIT_PARSER;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Use this class when Test are not FunctionTests */
@RunWith(Parameterized.class)
public abstract class AbstractMultiParserTestCase {

  @Parameterized.Parameter(value = 0)
  public String parser;

  private static final String AMF = "AMF";
  private static final String RAML = "RAML";

  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][] {{AMF}, {RAML}});
  }

  public boolean isAmf() {
    return AMF.equals(parser);
  }

  @Before
  public void beforeTest() {
    System.setProperty(MULE_APIKIT_PARSER, parser);
  }

  @After
  public void afterTest() {
    System.clearProperty(MULE_APIKIT_PARSER);
  }
}
