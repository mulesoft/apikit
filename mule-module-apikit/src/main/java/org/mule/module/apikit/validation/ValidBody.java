/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import java.util.Map;

import org.mule.runtime.api.message.MultiPartPayload;

public class ValidBody {
  Object payload;
  Object formParameters;

  public ValidBody(Object payload) {
    this.payload = payload;
  }

  public Object getPayload() {
    if(formParameters == null) {
      return payload;
    } else if(formParameters instanceof Map || formParameters instanceof MultiPartPayload){
      return formParameters;
    }
    return null;
  }

  public void setPayload(Object payload) {
    this.payload = payload;
  }

  public void setFormParameters(Object formParameters) {
    this.formParameters = formParameters;
  }

}
