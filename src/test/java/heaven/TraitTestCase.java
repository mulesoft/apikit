package heaven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import heaven.model.Heaven;
import heaven.model.parameter.QueryParameter;
import heaven.model.Resource;
import heaven.parser.HeavenParser;
import org.junit.Ignore;
import org.junit.Test;

public class TraitTestCase
{

    @Test @Ignore
    public void singleTrait()
    {
        HeavenParser parser = new HeavenParser();
        Heaven heaven = parser.parse("heaven/traits/single-trait.yaml");
        //FIXME
        //parser.validate(heaven);

        assertEquals("sample API", heaven.getTitle());
        assertFalse(heaven.getResources().isEmpty());

        Resource defaultResource = heaven.getResources().get("/default");
        assertEquals("Default", defaultResource.getName());
        assertEquals(1, defaultResource.getUses().size());
        assertEquals("paged", defaultResource.getUses().get(0));

        //trait applied
        Map<String,QueryParameter> queryParams = defaultResource.getAction("get").getQueryParameters();
        assertEquals(2, queryParams.size());
        assertTrue(queryParams.containsKey("q"));
        assertTrue(queryParams.containsKey("count"));

        QueryParameter q = queryParams.get("q");
        assertEquals("string", q.getType());
    }
}
