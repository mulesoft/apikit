package org.mule.module.apikit.transform;

import static org.mule.module.apikit.transform.JacksonTagResolver.JACKSON_TAG;
import static org.mule.module.apikit.transform.JaxbTagResolver.JAXB_TAG;

import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.visitor.NodeHandler;
import org.raml.parser.visitor.TagResolver;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

public class PojoValidatorTagResolver implements TagResolver
{

    @Override
    public boolean handles(Tag tag)
    {
        return JACKSON_TAG.equals(tag) || JAXB_TAG.equals(tag);
    }

    @Override
    public Node resolve(Node node, ResourceLoader resourceLoader, NodeHandler nodeHandler)
    {
        String className = ((ScalarNode) node).getValue();
        try
        {
            Thread.currentThread().getContextClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException e)
        {
            nodeHandler.onCustomTagError(node.getTag(), node, "Class not found " + className);
        }
        return node;
    }

}
