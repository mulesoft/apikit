package org.mule.module.apikit.transform;

import org.mule.module.apikit.exception.ApikitRuntimeException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;

import java.io.ByteArrayOutputStream;

import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.visitor.NodeHandler;
import org.raml.parser.visitor.TagResolver;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;


public class JacksonTagResolver implements TagResolver
{

    private static final Tag JACKSON_TAG = new Tag("!jackson");

    @Override
    public boolean handles(Tag tag)
    {
        return JACKSON_TAG.equals(tag);
    }

    @Override
    public Node resolve(Node node, ResourceLoader resourceLoader, NodeHandler nodeHandler)
    {
        String className = ((ScalarNode) node).getValue();
        try
        {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonSchema jsonSchema = objectMapper.generateJsonSchema(clazz);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            objectMapper.writeValue(baos, jsonSchema);
            String schema = baos.toString();
            return new ScalarNode(Tag.STR, schema, node.getStartMark(), node.getEndMark(), ((ScalarNode) node).getStyle());
        }
        catch (Exception e)
        {
            throw new ApikitRuntimeException(e);
        }
    }
}
