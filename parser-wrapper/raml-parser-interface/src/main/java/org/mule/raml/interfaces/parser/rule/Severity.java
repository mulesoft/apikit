package org.mule.raml.interfaces.parser.rule;

public enum Severity {
  INFO, WARNING, ERROR;

  public static Severity fromString(String severity) {
    return valueOf(severity.toUpperCase());
  }
}
