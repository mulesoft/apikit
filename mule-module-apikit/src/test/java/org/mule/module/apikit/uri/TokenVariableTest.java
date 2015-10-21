/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.uri;

import static org.junit.Assert.assertThat;

import com.google.common.collect.Maps;

import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;


public class TokenVariableTest
{

    private TokenVariable tokenVariable;

    private Variable variable;

    @Before
    public void setUp()
    {
        this.variable = Variable.parse("variable");
        this.tokenVariable = new TokenVariable(variable);
    }

    @Test
    public void testResolveSlashUpperCase() throws Exception
    {
        Map<Variable, Object> values = Maps.newHashMap();
        this.tokenVariable.resolve("%2F", values);
        assertThat(values, Matchers.<Variable, Object>hasEntry(variable, "/"));
    }

    @Test
    public void testResolveSlashLowerCase() throws Exception
    {
        Map<Variable, Object> values = Maps.newHashMap();
        this.tokenVariable.resolve("%2f", values);
        assertThat(values, Matchers.<Variable, Object>hasEntry(variable, "/"));
    }
}