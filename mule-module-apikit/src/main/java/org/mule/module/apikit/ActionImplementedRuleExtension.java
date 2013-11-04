/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.construct.Flow;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

import org.raml.model.Resource;
import org.raml.parser.rule.NodeRuleFactoryExtension;
import org.raml.parser.rule.TupleRule;

public class ActionImplementedRuleExtension implements NodeRuleFactoryExtension
{

    private final Map<String, Flow> restFlowMap;

    public ActionImplementedRuleExtension(Map<String, Flow> restFlowMap)
    {
        this.restFlowMap = restFlowMap;
    }

    @Override
    public boolean handles(Field field, Annotation annotation)
    {
        return field.getName().equals("actions") && field.getDeclaringClass().equals(Resource.class);
    }

    @Override
    public TupleRule<?, ?> createRule(Field field, Annotation annotation)
    {
        return new ActionNotImplementedTupleRule(restFlowMap);
    }
}
