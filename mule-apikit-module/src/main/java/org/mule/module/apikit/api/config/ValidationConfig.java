/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.config;

import java.util.concurrent.ExecutionException;

import javax.xml.validation.Schema;

import org.mule.module.apikit.api.validation.ApiKitJsonSchema;

public interface ValidationConfig {

  boolean isParserV2();
  ApiKitJsonSchema getJsonSchema(String schemaPath) throws ExecutionException;
  Schema getXmlSchema( String schemaPath) throws ExecutionException;
}
