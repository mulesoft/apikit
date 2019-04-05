/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

public class CompositeErrorTestCase {

  @Test
  public void compositeErrorWithoutParsingErrorType() {
    List<SimpleScaffoldingError> child = Arrays.asList(new SimpleScaffoldingError("cause1"));
    CompositeScaffoldingError compositeErrorChild =
        new CompositeScaffoldingError("description", ScaffoldingErrorType.RAML, child);
    assertThat(compositeErrorChild.cause(), is("description:\ncause1"));

    List<SimpleScaffoldingError> children =
        Arrays.asList(new SimpleScaffoldingError("cause1"), new SimpleScaffoldingError("cause2"),
                      new SimpleScaffoldingError("cause3"));
    CompositeScaffoldingError compositeParsingErrorChildren =
        new CompositeScaffoldingError("description", ScaffoldingErrorType.GENERATION, children);
    assertThat(compositeParsingErrorChildren.cause(), is("description:\ncause1\ncause2\ncause3"));
    assertThat(compositeParsingErrorChildren.errorType(), is(ScaffoldingErrorType.GENERATION));
  }

  @Test
  public void compositeErrorWithChildrenAndParsingErrorType() {
    List<SimpleScaffoldingError> children =
        Arrays.asList(new SimpleScaffoldingError("cause1"), new SimpleScaffoldingError("cause2"),
                      new SimpleScaffoldingError("cause3"));
    CompositeScaffoldingError compositeError =
        new CompositeScaffoldingError("description", ScaffoldingErrorType.AMF, children);
    assertThat(compositeError.cause(), is("description:\ncause1\ncause2\ncause3"));
    assertThat(compositeError.errorType(), is(ScaffoldingErrorType.AMF));
  }

  @Test
  public void simpleTest() {
    assertThat(new SimpleScaffoldingError("cause").cause(), Matchers.is("cause"));
    assertThat(new SimpleScaffoldingError("cause").errorType(), Matchers.is(ScaffoldingErrorType.GENERATION));
    assertThat(new SimpleScaffoldingError("cause", ScaffoldingErrorType.RAML).errorType(),
               Matchers.is(ScaffoldingErrorType.RAML));
    assertThat(new SimpleScaffoldingError("cause", ScaffoldingErrorType.AMF).errorType(),
               Matchers.is(ScaffoldingErrorType.AMF));
  }
}
