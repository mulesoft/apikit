/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.core.api.exception.TypedException;

import org.raml.model.ParamType;
import org.raml.model.parameter.AbstractParam;

public class UrlEncodedFormValidatorTestCase
{
    @Test
    public void validURlEncodedFormParamsV1() throws MuleRestException
    {
        Map<String, String> payload = new HashMap<>();;
        payload.put("must", "true");

        IMimeType mimeType = Mockito.mock(IMimeType.class);
        when(mimeType.getType()).thenReturn("application/x-www-form-urlencoded");

        AbstractParam abstractParam = new AbstractParam("must", ParamType.BOOLEAN, true);
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

    @Test (expected = TypedException.class)
    public void missingParamURlEncodedFormParamsV1() throws MuleRestException
    {
        Map<String, String> payload = new HashMap<>();;
        payload.put("must", "true");

        IMimeType mimeType = Mockito.mock(IMimeType.class);
        when(mimeType.getType()).thenReturn("application/x-www-form-urlencoded");

        AbstractParam abstractParam = new AbstractParam("must", ParamType.BOOLEAN, true);
        abstractParam.setDefaultValue("true");
        IParameter param = new org.mule.raml.implv1.model.parameter.ParameterImpl(abstractParam);


        List<IParameter> paramList = new ArrayList<>();
        paramList.add(param);

        Map<String, List<IParameter>> map = new HashMap<>();
        map.put("other", paramList);

        when(mimeType.getFormParameters()).thenReturn(map);

        UrlencodedFormValidator urlEncodedFormvalidator = new UrlencodedFormValidator(mimeType.getFormParameters());

        urlEncodedFormvalidator.validate(payload);
    }

    @Test
    public void useDefaultParamURlEncodedFormParamsV1() throws MuleRestException
    {
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

    @Test(expected = TypedException.class )
    public void expectedValueURlEncodedFormParamsV1() throws MuleRestException
    {
        Map<String, String> payload = new HashMap<>();;
        payload.put("must", "other");

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

        urlEncodedFormvalidator.validate(payload);

    }
}
