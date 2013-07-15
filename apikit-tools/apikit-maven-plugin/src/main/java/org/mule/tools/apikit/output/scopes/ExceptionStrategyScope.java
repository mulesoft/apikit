package org.mule.tools.apikit.output.scopes;


import org.jdom2.Element;
import org.mule.tools.apikit.misc.APIKitTools;

import java.util.Arrays;
import java.util.List;

import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

public class ExceptionStrategyScope implements Scope {
    private final Element exceptionStrategy;

    public static class StatusCodeMapping {
        private final int statusCode;
        private final String exception;
        private final String msg;

        public StatusCodeMapping(int statusCode, String exception, String msg) {

            this.statusCode = statusCode;
            this.exception = exception;
            this.msg = msg;
        }

        public String getStatusCode() {
            return Integer.toString(statusCode);
        }

        public String getException() {
            return exception;
        }

        public String getMsg() {
            return msg;
        }
    }

    public ExceptionStrategyScope(Element mule) {
        List<StatusCodeMapping> statusCodeMappings = Arrays.asList(
                new StatusCodeMapping(404, "org.mule.module.apikit.exception.NotFoundException", "resource not found"),
                new StatusCodeMapping(405, "org.mule.module.apikit.exception.MethodNotAllowedException", "method not allowed"),
                new StatusCodeMapping(415, "org.mule.module.apikit.exception.UnsupportedMediaTypeException", "unsupported media type"),
                new StatusCodeMapping(406, "org.mule.module.apikit.exception.NotAcceptableException", "not acceptable"),
                new StatusCodeMapping(400, "org.mule.module.apikit.exception.BadRequestException", "bad request")
        );

        exceptionStrategy = new Element("mapping-exception-strategy",
                APIKitTools.API_KIT_NAMESPACE.getNamespace());

        exceptionStrategy.setAttribute("name", "Global_Mapping_Exception_Strategy");

        for (StatusCodeMapping statusCodeMapping : statusCodeMappings) {
            Element mapping = new Element("mapping",
                    APIKitTools.API_KIT_NAMESPACE.getNamespace());

            mapping.setAttribute("statusCode", statusCodeMapping.getStatusCode());

            Element exception = new Element("exception", APIKitTools.API_KIT_NAMESPACE.getNamespace());
            exception.setAttribute("value", statusCodeMapping.getException());
            mapping.addContent(exception);

            Element setPayload = new Element("set-payload", XMLNS_NAMESPACE.getNamespace());
            setPayload.setAttribute("value", statusCodeMapping.getMsg());
            mapping.addContent("\n            ");
            mapping.addContent(setPayload);

            exceptionStrategy.addContent(mapping);
        }

        mule.addContent("\n    ");
        mule.addContent(exceptionStrategy);
    }

    @Override
    public Element generate() {
        return exceptionStrategy;
    }
}
