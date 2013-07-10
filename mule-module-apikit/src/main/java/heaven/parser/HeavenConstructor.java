package heaven.parser;

import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

public class HeavenConstructor extends SafeConstructor
{

    public HeavenConstructor()
    {
        this.yamlConstructors.put(new Tag("tag:heaven-lang.org,1.0:include"), new ConstructInclude());
        this.yamlConstructors.put(new Tag("tag:heaven-lang.org,1.0:param"), new ConstructParam());
    }

    public static YAMLException createYAMLException(Node node, Exception e)
    {
        return new YAMLException(getErrorLocation(node), e);
    }
    public static YAMLException createYAMLException(Node node, String message)
    {
        return new YAMLException(getErrorLocation(node) + " -- " + message);
    }

    private static String getErrorLocation(Node node)
    {
        Mark mark = node.getStartMark();
        String location = String.format("error in line %d, column %d:", mark.getLine(), mark.getColumn());
        return location;
    }
}
