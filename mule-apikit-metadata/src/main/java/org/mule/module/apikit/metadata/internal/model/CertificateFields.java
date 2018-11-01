/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

public enum CertificateFields {
  CLIENT_CERTIFICATE_ENCODED("encoded"),
  CLIENT_CERTIFICATE_PUBLIC_KEY("publicKey"),
  CLIENT_CERTIFICATE_TYPE("type");

  private String name;

  CertificateFields(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
