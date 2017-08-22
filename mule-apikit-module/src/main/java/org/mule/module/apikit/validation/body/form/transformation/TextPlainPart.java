/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

public class TextPlainPart implements MultipartPart {

  String name;
  String value;
  String template =
      "'<<name>>':{headers: {'Content-Disposition': {name: \"<<name>>\"}, 'Content-Type': \"text/plain\"}, content: \"<<value>>\" }";

  public TextPlainPart() {
    name = "part";
    value = "";
  }

  public TextPlainPart setName(String name) {
    if (name != null) {
      this.name = name;
    }
    return this;
  }

  public String getName() {
    return this.name;
  }

  public TextPlainPart setValue(String value) {
    if (value != null) {
      this.value = value;
    }
    return this;
  }

  public String getValue() {
    return this.value;
  }

  public String toDataWeaveString() {
    return template.replace("<<name>>", name).replace("<<value>>", value);
  }
}
