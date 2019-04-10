/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mule.tools.apikit.model.Status.FAILED;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.mule.parser.service.CompositeScaffoldingError;
import org.mule.parser.service.ScaffoldingErrorType;
import org.mule.tools.apikit.model.ScaffolderReport;

public class ScaffolderReportTestCase {

  @Test
  public void testBuilder() {
    ScaffolderReport report =
        new ScaffolderReport.Builder()
            .withStatus(FAILED)
            .withVendorId("vendor_id")
            .withVersion("version")
            .withScaffoldingErrors(
                                   Arrays.asList(
                                                 new CompositeScaffoldingError(
                                                                               "description", ScaffoldingErrorType.AMF,
                                                                               Collections.emptyList())))
            .build();

    assertThat(report.getStatus(), is("FAILED"));
    assertThat(report.getVendorId(), is("vendor_id"));
    assertThat(report.getVersion(), is("version"));
    assertThat(report.getScaffoldingErrorsByType(ScaffoldingErrorType.AMF).size(), is(1));
    assertThat(report.getScaffoldingErrorsByType(ScaffoldingErrorType.RAML).size(), is(0));
    assertThat(report.getScaffoldingErrorsByType(ScaffoldingErrorType.GENERATION).size(), is(0));
    assertThat(report.getScaffoldingErrors().size(), is(1));
  }
}
