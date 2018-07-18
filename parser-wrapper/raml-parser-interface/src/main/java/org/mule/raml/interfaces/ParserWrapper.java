/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces;

import org.mule.raml.interfaces.injector.IRamlUpdater;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.rule.IValidationReport;

public interface ParserWrapper {

  ApiVendor getApiVendor();

  void validate();

  IValidationReport validationReport();

  IRaml build();

  String dump(String ramlContent, IRaml api, String oldSchemeHostPort, String newSchemeHostPort);

  String dump(IRaml api, String newBaseUri);

  IRamlUpdater getRamlUpdater(IRaml api);

  void updateBaseUri(IRaml api, String baseUri);
}
