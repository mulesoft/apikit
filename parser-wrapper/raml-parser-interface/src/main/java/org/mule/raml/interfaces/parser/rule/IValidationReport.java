/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.parser.rule;

import java.util.List;

import static org.mule.raml.interfaces.parser.rule.Severity.ERROR;

public interface IValidationReport {


  default boolean conforms() {
    return getResults().stream().noneMatch(r -> r.getSeverity().equals(ERROR));
  }

  List<IValidationResult> getResults();
}
