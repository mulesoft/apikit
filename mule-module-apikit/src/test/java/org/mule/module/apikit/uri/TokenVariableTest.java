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