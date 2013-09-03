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
