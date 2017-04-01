/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body;

import static org.mockito.Mockito.when;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.raml.implv2.v10.model.MimeTypeImpl;
import org.mule.raml.implv2.v10.model.ParameterImpl;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.message.Message;
import org.mule.service.http.api.domain.ParameterMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.raml.model.ParamType;
import org.raml.model.parameter.AbstractParam;

public class FormParametersValidatorTestCase
{
    @Test
    public void validFormParamsV1() throws MuleRestException
    {
        Map paramsMap = new HashMap<>();
        paramsMap.put("must", "true");
        Message.Builder messageBuilder = Message.builder().payload(paramsMap);
        ParameterMap headers = new ParameterMap();
        headers.put("content-type", "application/x-www-form-urlencoded");
        HttpRequestAttributes attributes = new HttpRequestAttributes(headers, null, null, null, null, null, null, null, null, null, null, null, null);
        messageBuilder.attributes(attributes);

        Message message = messageBuilder.build();

        IMimeType mimeType = Mockito.mock(IMimeType.class);
        when(mimeType.getType()).thenReturn("application/x-www-form-urlencoded");

        //IParameter param = Mockito.mock(IParameter.class);
        //when(param.getDisplayName()).thenReturn("must");
        //when(param.getDefaultValue()).thenReturn("true");
        AbstractParam abstractParam = new AbstractParam("must", ParamType.BOOLEAN, true);
        abstractParam.setDefaultValue("true");
        IParameter param = new org.mule.raml.implv1.model.parameter.ParameterImpl(abstractParam);


        List<IParameter> paramList = new ArrayList<>();
        paramList.add(param);

        Map<String, List<IParameter>> map = new HashMap<>();
        map.put("must", paramList);

        when(mimeType.getFormParameters()).thenReturn(map);

        FormParametersValidator validator = new FormParametersValidator(mimeType, false);
        validator.validate(message);
    }
}
