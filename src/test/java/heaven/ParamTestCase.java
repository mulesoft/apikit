package heaven;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import heaven.parser.ConstructParam;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;

public class ParamTestCase
{
    @Test
    public void sequenceKeyAndScalarValue()
    {
        Yaml yaml = new Yaml(new ParamConstructor());
        Object data = yaml.load(this.getClass().getClassLoader().getResourceAsStream("heaven/param-sequence.yaml"));
        Map<String, ?> map = (Map<String, ?>) data;
        Map trait = (Map) ((Map) map.get("traits")).get("paged");

        List<String> methods = (List<String>) ((Map) trait.get("requires")).get("method");
        assertEquals(2, methods.size());
        assertEquals("get", methods.get(0));
        assertEquals("post", methods.get(1));

        String example = (String) ((Map) trait.get("requires")).get("example");
        assertEquals("1", example);
    }

    private static class ParamConstructor extends SafeConstructor
    {
        ParamConstructor()
        {
            this.yamlConstructors.put(new Tag("tag:heaven-lang.org,1.0:param"), new ConstructParam());
        }
    }

}
