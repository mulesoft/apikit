package heaven;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import heaven.model.Heaven;
import heaven.parser.HeavenParser;
import heaven.parser.ParseException;

public abstract class AbstractHeavenTestCase
{


    protected Heaven validateFile(String fileName)
    {
        HeavenParser parser = new HeavenParser();
        Heaven heaven = parser.parse(fileName);
        return heaven;
    }

    protected void expectParseException(String fileName, String exceptionMessage)
    {
        try
        {
            validateFile(fileName);
            fail();
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), containsString(exceptionMessage));
        }
    }
}
