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

  public OutputRepresentationHandler(HttpProtocolAdapter adapter) {
    this.adapter = adapter;
  }

  public String negotiateOutputRepresentation(IAction action, List<String> mimeTypes) throws MuleRestException {
    if (action == null || action.getResponses() == null || mimeTypes.isEmpty()) {
      //no response media-types defined, return no body
      return null;
    }
    MediaType bestMatch = bestMatch(mimeTypes, adapter.getAcceptableResponseMediaTypes());
    if (bestMatch == null) {
      throw new NotAcceptableException();
    }
    for (String representation : mimeTypes) {
      if (representation.equalsIgnoreCase(bestMatch.withoutParameters().toString())) {
        return representation;
      }
    }
    throw new NotAcceptableException();
  }

}
