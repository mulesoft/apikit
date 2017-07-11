/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.raml.implv2.v10.model.MimeTypeImpl;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.core.api.exception.TypedException;

import org.raml.model.ParamType;
import org.raml.model.parameter.AbstractParam;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

public class UrlEncodedFormValidatorV2TestCase {

  @Test
  public void validURlEncodedFormParamsV2() throws MuleRestException {
    Map<String, String> payload = new HashMap<>();;
    payload.put("must", "true");

    TypeDeclaration typeDeclaration = Mockito.mock(TypeDeclaration.class);
    MimeTypeImpl mimeType = new MimeTypeImpl(typeDeclaration);

    UrlencodedFormV2Validator urlEncodedFormvalidator = new UrlencodedFormV2Validator(mimeType);

    Map<String, String> validatedParams = urlEncodedFormvalidator.validate(payload);

    assertEquals(validatedParams.get("must"), "true");
  }

  @Test (expected = TypedException.class)
  public void invalidURlEncodedFormParamsV2() throws MuleRestException {
    Map<String, String> payload = new HashMap<>();;
    payload.put("must", null);

    TypeDeclaration typeDeclaration = Mockito.mock(TypeDeclaration.class);

    ValidationResult result = Mockito.mock(ValidationResult.class);
    when(result.getMessage()).thenReturn("error");

    when(typeDeclaration.validate(anyString())).thenReturn(Arrays.asList(result));

    MimeTypeImpl mimeType = new MimeTypeImpl(typeDeclaration);

    UrlencodedFormV2Validator urlEncodedFormvalidator = new UrlencodedFormV2Validator(mimeType);

    urlEncodedFormvalidator.validate(payload);
  }

  @Test
  public void skipValidationUrlEncodedFormParamsV2() throws MuleRestException {
    Map<String, String> payload = new HashMap<>();;
    payload.put("must", null);

    IMimeType mimeType = Mockito.mock(IMimeType.class);
    when(mimeType.getType()).thenReturn("application/x-www-form-urlencoded");

    AbstractParam abstractParam = new AbstractParam("must", ParamType.BOOLEAN, false);
    abstractParam.setDefaultValue("true");
    IParameter param = new org.mule.raml.implv1.model.parameter.ParameterImpl(abstractParam);


    List<IParameter> paramList = new ArrayList<>();
    paramList.add(param);

    Map<String, List<IParameter>> map = new HashMap<>();
    map.put("must", paramList);

    when(mimeType.getFormParameters()).thenReturn(map);

    UrlencodedFormValidator urlEncodedFormvalidator = new UrlencodedFormValidator(mimeType.getFormParameters());

    Map<String, String> validatedParams = urlEncodedFormvalidator.validate(payload);

    assertEquals(validatedParams.get("must"), "true");
  }

}
