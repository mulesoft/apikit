package heaven.parser;

import static heaven.parser.HeavenConstructor.createYAMLException;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

public class ConstructInclude extends AbstractConstruct
{

    public Object construct(Node node)
    {
        String val = ((ScalarNode) node).getValue();
        try
        {
            //TODO handle URLs
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(val);
            if (inputStream == null)
            {
                throw createYAMLException(node, "ERROR: file not found " + val);
            }
            if (val.endsWith(".yaml") || val.endsWith(".yml"))
            {
                Yaml yaml = new Yaml(new HeavenConstructor());
                Object data = yaml.load(inputStream);
                return data;
            }
            return IOUtils.toString(inputStream);
        }
        catch (IOException e)
        {
            throw createYAMLException(node, "ERROR: processing file " + val);
        }
    }
}
