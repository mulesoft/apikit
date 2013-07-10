package heaven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import heaven.parser.ConstructInclude;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Tag;

public class IncludeTestCase
{

    @Test
    @SuppressWarnings("unchecked")
    public void testNotFound()
    {
        Yaml yaml = new Yaml(new IncludeConstructor());
        try
        {
            yaml.load(this.getClass().getClassLoader().getResourceAsStream("heaven/include/include-not-found.yaml"));
            fail();
        }
        catch (YAMLException e)
        {
            assertTrue(e.getMessage().contains("ERROR: file not found"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleLineString()
    {
        Yaml yaml = new Yaml(new IncludeConstructor());
        Object data = yaml.load(this.getClass().getClassLoader().getResourceAsStream("heaven/include/include-non-yaml-single-line.yaml"));
        Map<String, String> map = (Map<String, String>) data;
        assertEquals("included title", map.get("title"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultiLineString() throws Exception
    {
        Yaml yaml = new Yaml(new IncludeConstructor());
        Object data = yaml.load(this.getClass().getClassLoader().getResourceAsStream("heaven/include/include-non-yaml-multi-line.yaml"));
        Map<String, String> map = (Map<String, String>) data;
        String multiLine = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("heaven/include/include-non-yaml-multi-line.txt"));
        assertEquals(multiLine, map.get("document"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testYaml()
    {
        Yaml yaml = new Yaml(new IncludeConstructor());
        Object data = yaml.load(this.getClass().getClassLoader().getResourceAsStream("heaven/include/include-yaml.yaml"));
        Map<String, Object> map = (Map<String, Object>) data;
        assertTrue(map.get("resources") instanceof List);
        List resources = (List) map.get("resources");
        Map resource = (Map) resources.get(0);
        assertEquals("Jobs", resource.get("name"));
        assertEquals("jobs", resource.get("relativeUri"));
    }

    @Test
    @Ignore //TODO not supoorted
    @SuppressWarnings("unchecked")
    public void testYamlWithReference()
    {
        Yaml yaml = new Yaml(new IncludeConstructor());
        Object data = yaml.load(this.getClass().getClassLoader().getResourceAsStream("heaven/include/include-yaml-reference.yaml"));
        Map<String, Object> map = (Map<String, Object>) data;
        assertTrue(map.get("address1") instanceof Map);
        assertTrue(map.get("address2") instanceof Map);
        Map address1 = (Map) map.get("address1");
        Map home = (Map) address1.get("home");
        Map address2 = (Map) map.get("address2");
        Map billing = (Map) address2.get("billing");
        assertEquals(home.get("street"), billing.get("street"));
        assertEquals(home.get("city"), billing.get("city"));
    }

    private static class IncludeConstructor extends SafeConstructor
    {
        IncludeConstructor()
        {
            this.yamlConstructors.put(new Tag("tag:heaven-lang.org,1.0:include"), new ConstructInclude());
        }
    }
}
