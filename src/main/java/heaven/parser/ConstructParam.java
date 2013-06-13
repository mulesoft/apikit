package heaven.parser;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.nodes.Node;

public class ConstructParam extends AbstractConstruct
{

    @Override
    public Object construct(Node node)
    {
        //don't do anything, handled during merge phase
        return node;
    }
}
