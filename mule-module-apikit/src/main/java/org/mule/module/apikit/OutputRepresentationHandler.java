/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.module.apikit.RestContentTypeParser.bestMatch;

import com.google.common.net.MediaType;
import java.util.List;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.raml.interfaces.model.IAction;

public class OutputRepresentationHandler {
  private final HttpProtocolAdapter adapter;
  private final boolean throwNotAcceptable;

  public OutputRepresentationHandler(HttpProtocolAdapter adapter, boolean throwNotAcceptable) {
    this.adapter = adapter;
    this.throwNotAcceptable = throwNotAcceptable;
  }

  public String negotiateOutputRepresentation(IAction action, List<String> mimeTypes) throws MuleRestException {
    if (action == null || action.getResponses() == null || mimeTypes.isEmpty()) {
      //no response media-types defined, return no body
      return null;
    }
    MediaType bestMatch = bestMatch(mimeTypes, adapter.getAcceptableResponseMediaTypes());
    if (bestMatch == null) {
      return handleNotAcceptable();
    }
    for (String representation : mimeTypes) {
      if (representation.equalsIgnoreCase(bestMatch.withoutParameters().toString())) {
        return representation;
      }
    }
    return handleNotAcceptable();
  }

  private String handleNotAcceptable() throws NotAcceptableException {
    if (throwNotAcceptable) {
      throw new NotAcceptableException();
    }
    return null;
  }

}
