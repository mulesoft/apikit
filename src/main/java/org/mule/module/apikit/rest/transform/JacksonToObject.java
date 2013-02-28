package org.mule.module.apikit.rest.transform;

import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.CoreMessages;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A transformer that will convert a JSON encoded object graph to a java object. The
 * object type is determined by the 'returnType' attribute. Note that this
 * transformers supports Arrays and Lists. For example, to convert a JSON string to
 * an array of org.foo.Person, set the the returnClass=[Lorg.foo.Person;.
 */
public class JacksonToObject extends AbstractJacksonTransformer
{

    private static final DataType<JacksonData> JSON_TYPE = DataTypeFactory.create(JacksonData.class);

    private Map<Class<?>, Class<?>> deserializationMixins = new HashMap<Class<?>, Class<?>>();

    public JacksonToObject()
    {
        this.registerSourceType(DataTypeFactory.create(Reader.class));
        this.registerSourceType(DataTypeFactory.create(URL.class));
        this.registerSourceType(DataTypeFactory.create(File.class));
        this.registerSourceType(DataTypeFactory.STRING);
        this.registerSourceType(DataTypeFactory.INPUT_STREAM);
        this.registerSourceType(DataTypeFactory.BYTE_ARRAY);
        setReturnDataType(JSON_TYPE);
    }

    @Override
    public void initialise() throws InitialisationException
    {
        super.initialise();
    }

    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException
    {
        Object src = message.getPayload();
        Object returnValue;
        InputStream is = null;
        Reader reader = null;

        try
        {
            if (src instanceof InputStream)
            {
                is = (InputStream) src;
            }
            else if (src instanceof File)
            {
                is = new FileInputStream((File) src);
            }
            else if (src instanceof URL)
            {
                is = ((URL) src).openStream();
            }
            else if (src instanceof byte[])
            {
                is = new ByteArrayInputStream((byte[]) src);
            }

            if (src instanceof Reader)
            {
                if (getReturnDataType().equals(JSON_TYPE))
                {
                    returnValue = new JacksonData((Reader) src);
                }
                else
                {
                    returnValue = getMapper().readValue((Reader) src, getReturnDataType().getType());
                }
            }
            else if (src instanceof String)
            {
                if (getReturnDataType().equals(JSON_TYPE))
                {
                    returnValue = new JacksonData((String) src);
                }
                else
                {
                    returnValue = getMapper().readValue((String) src, getReturnDataType().getType());
                }
            }
            else
            {
                reader = new InputStreamReader(is, outputEncoding);
                if (getReturnDataType().equals(JSON_TYPE))
                {
                    returnValue = new JacksonData(reader);
                }
                else
                {
                    returnValue = getMapper().readValue(reader, getReturnDataType().getType());
                }
            }
            return returnValue;
        }
        catch (Exception e)
        {
            throw new TransformerException(CoreMessages.transformFailed("json",
                                                                        getReturnDataType().getType().getName()), this, e);
        }
        finally
        {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(is);
        }
    }
}

