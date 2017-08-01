import org.mule.module.metadata.interfaces.ResourceLoader;

import java.io.File;
import java.net.URISyntaxException;

public class TestResourceLoader implements ResourceLoader
{

    @Override
    public File getRamlResource(String relativePath)
    {
        try
        {
            return new File(TestResourceLoader.class.getResource(relativePath).toURI());

        } catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        return null;
    }

}
