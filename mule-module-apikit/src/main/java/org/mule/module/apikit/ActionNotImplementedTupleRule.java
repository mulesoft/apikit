package org.mule.module.apikit;

import static org.raml.parser.rule.ValidationResult.Level.WARN;

import org.mule.construct.Flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.raml.parser.resolver.DefaultTupleHandler;
import org.raml.parser.rule.DefaultTupleRule;
import org.raml.parser.rule.TupleRule;
import org.raml.parser.rule.ValidationResult;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

public class ActionNotImplementedTupleRule extends DefaultTupleRule
{

    private final Map<String, Flow> restFlowMap;

    public ActionNotImplementedTupleRule(Map<String, Flow> restFlowMap)
    {
        super("actions", new DefaultTupleHandler());
        this.restFlowMap = restFlowMap;
    }

    @Override
    public List<ValidationResult> validateKey(Node key)
    {
        String action = ((ScalarNode) key).getValue();
        StringBuilder resource = new StringBuilder();

        TupleRule<?, ?> parent = getParentTupleRule();
        while (parent.getParentTupleRule() != null)
        {
            resource.insert(0, ((ScalarNode) parent.getKey()).getValue());
            parent = parent.getParentTupleRule();
        }

        List<ValidationResult> result = new ArrayList<ValidationResult>();
        if (restFlowMap == null || restFlowMap.get(action + ":" + resource) == null)
        {
            result.add(ValidationResult.create(WARN, String.format("Resource-action pair has no implementation -> %s:%s ",
                                                                   resource, action)));
        }
        return result;
    }

}
