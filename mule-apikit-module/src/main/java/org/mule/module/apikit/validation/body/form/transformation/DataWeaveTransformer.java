/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.module.apikit.input.stream.RewindableInputStream;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.el.ExpressionManager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mule.apikit.common.CommonUtils.cast;
import static org.mule.runtime.api.metadata.MediaType.create;

public class DataWeaveTransformer {

  private static final MediaType MULTIPART_FORMDATA = create("multipart", "form-data");

  private static final Logger LOGGER = LoggerFactory.getLogger(DataWeaveTransformer.class);

  private final DataType multiMapDataType = DataType.builder()
      .mapType(MultiMap.class)
      .keyType(String.class)
      .valueType(String.class)
      .build();

  private ExpressionManager expressionManager;

  public DataWeaveTransformer(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

  public TypedValue runDataWeaveScript(String script, DataType dataType, TypedValue payload)
      throws InvalidFormParameterException {
    BindingContext.Builder bindingContextBuilder = BindingContext.builder();

    bindingContextBuilder.addBinding("payload", payload);
    TypedValue result;
    try {
      if (dataType != null) {
        result = expressionManager.evaluate(script, dataType, bindingContextBuilder.build());
      } else {
        result = expressionManager.evaluate(script, bindingContextBuilder.build());
      }
    } catch (Exception e) {
      LOGGER.error("Invalid form parameter exception. Payload transformation could not be performed. Reason: " + e.getMessage());
      throw new InvalidFormParameterException("Invalid form parameter exception. Payload transformation could not be performed. Reason: "
          + e.getMessage());
    }
    return result;
  }

  public MultiMap<String, String> getMultiMapFromPayload(TypedValue payload) throws InvalidFormParameterException {
    final String script;

    final MediaType mediaType = payload.getDataType().getMediaType();
    if (mediaType.matches(MULTIPART_FORMDATA)) {
      script = "output application/java --- {(payload.parts pluck { '$$': $.content })}";
    } else {
      script = "output application/java --- payload";
    }

    final MultiMap<String, String> result = cast(runDataWeaveScript(script, multiMapDataType, payload).getValue());

    // Rewind input stream, if possible.
    // This rewind is needed to be able to consume the stream several times
    if (payload.getValue() instanceof RewindableInputStream) {
      ((RewindableInputStream) payload.getValue()).rewind();
    }

    return result;
  }

  public TypedValue getXFormUrlEncodedStream(MultiMap<String, String> mapToTransform, DataType responseDataType)
      throws InvalidFormParameterException {
    TypedValue<MultiMap<String, String>> modifiedPayload = new TypedValue<>(mapToTransform, multiMapDataType);
    String script = "output application/x-www-form-urlencoded --- payload";
    return runDataWeaveScript(script, responseDataType, modifiedPayload);
  }

  public List<String> getKeysFromPayload(TypedValue payload) throws InvalidFormParameterException {
    String script = "output application/java --- payload.parts pluck $$ as String";
    return cast(runDataWeaveScript(script, null, payload).getValue());
  }
}
