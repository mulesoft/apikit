package heaven;

import org.junit.Test;

public class ResourceValidationTestCase extends AbstractHeavenTestCase
{

    @Test
    public void resourceNotDefined()
    {
        expectParseException("heaven/resource-not-defined.yaml", "at least one resource must be defined");
    }

    @Test
    public void resourceDuplicate()
    {
        expectParseException("heaven/resource-dup.yaml", "Duplicate resource paths");
    }

    @Test
    public void resourceOk()
    {
        validateFile("heaven/resource-nested.yaml");
    }

}
