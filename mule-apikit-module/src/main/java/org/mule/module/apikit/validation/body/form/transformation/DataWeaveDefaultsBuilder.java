/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import java.util.ArrayList;
import java.util.List;

public class DataWeaveDefaultsBuilder {

  List<MultipartPart> partsToAppend;
  private final String template =
      "output multipart/form-data --- {preamble: payload.preamble default '', parts: payload.parts ++ {<<defaults>>}}";

  public DataWeaveDefaultsBuilder() {
    partsToAppend = new ArrayList<>();
  }

  public void addPart(MultipartPart multipartPart) {
    partsToAppend.add(multipartPart);
  }

  public boolean areDefaultsToAdd() {
    return partsToAppend.size() > 0;
  }

  public String build() {
    if (partsToAppend.size() == 0) {
      return "payload";
    }
    String codeToAppend = "";
    for (MultipartPart part : partsToAppend) {
      codeToAppend += part.toDataWeaveString() + ",";
    }
    codeToAppend = codeToAppend.substring(0, codeToAppend.length() - 1);
    return template.replace("<<defaults>>", codeToAppend);
  }
}
