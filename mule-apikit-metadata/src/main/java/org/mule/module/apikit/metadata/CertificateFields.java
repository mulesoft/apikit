package org.mule.module.apikit.metadata;

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
