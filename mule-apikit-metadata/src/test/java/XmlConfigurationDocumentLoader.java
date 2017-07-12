import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.config.spring.MuleDocumentLoader;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;

public class XmlConfigurationDocumentLoader {

    private final static int NO_VALIDATION_XSD = 0;
    private final static int VALIDATION_XSD = 3;

    public Document loadDocument(InputStream inputStream) {
        try {
            Document document =
                    new MuleDocumentLoader().loadDocument(new InputSource(inputStream),
                            new DelegatingEntityResolver(Thread.currentThread().getContextClassLoader()),
                            new DefaultHandler(), NO_VALIDATION_XSD, true);
            return document;
        } catch (Exception e) {
            throw new MuleRuntimeException(e);
        }
    }
}