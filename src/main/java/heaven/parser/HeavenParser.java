package heaven.parser;

import org.mule.module.apikit.rest.uri.URIPattern;

import java.util.List;
import java.util.Map;

import heaven.model.Heaven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class HeavenParser
{

    protected static final Logger logger = LoggerFactory.getLogger("heaven.parser.HeavenParser");

    public Heaven parse(String file)
    {
        Yaml yaml = new Yaml(new HeavenConstructor());
        Map yamlMap = (Map) yaml.load(this.getClass().getClassLoader().getResourceAsStream(file));
        return new Heaven(yamlMap);
    }

    //TODO apply validation to templated URIs
    public static boolean validTemplate(List<?> template)
    {
        try
        {
            URIPattern pattern = new URIPattern((String) template.get(0));
            logger.debug("pattern parsed: " + pattern);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
