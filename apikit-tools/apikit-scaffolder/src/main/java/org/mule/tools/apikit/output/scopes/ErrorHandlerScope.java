/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import static org.mule.tools.apikit.output.MuleConfigGenerator.EE_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XSI_NAMESPACE;

import org.jdom2.CDATA;

import java.util.Arrays;
import java.util.List;

import org.jdom2.Element;

public class ErrorHandlerScope implements Scope {

    private Element errorHandler;

    public static ErrorHandlerScope createForConsoleFlow() {
        List<StatusCodeMapping> statusCodeMappings = Arrays.asList(
                new StatusCodeMapping(404, "APIKIT:NOT_FOUND", "Resource not found")
        );

        return new ErrorHandlerScope(statusCodeMappings);
    }

    public static ErrorHandlerScope createForMainFlow() {
        List<StatusCodeMapping> statusCodeMappings = Arrays.asList(
                new StatusCodeMapping(400, "APIKIT:BAD_REQUEST", "Bad request"),
                new StatusCodeMapping(404, "APIKIT:NOT_FOUND", "Resource not found"),
                new StatusCodeMapping(405, "APIKIT:METHOD_NOT_ALLOWED", "Method not allowed"),
                new StatusCodeMapping(406, "APIKIT:NOT_ACCEPTABLE", "Not acceptable"),
                new StatusCodeMapping(415, "APIKIT:UNSUPPORTED_MEDIA_TYPE", "Unsupported media type")
        );

        return new ErrorHandlerScope(statusCodeMappings);
    }

    private ErrorHandlerScope(List<StatusCodeMapping> statusCodeMappings) {
        createErrorHandlerElement(statusCodeMappings);
    }

    private void createErrorHandlerElement(List<StatusCodeMapping> statusCodeMappings)
    {
        errorHandler = new Element("error-handler", XMLNS_NAMESPACE.getNamespace());

        for (StatusCodeMapping statusCodeMapping : statusCodeMappings)
        {
            Element errorMapping = new Element("on-error-propagate", XMLNS_NAMESPACE.getNamespace());
            errorMapping.setAttribute("type", statusCodeMapping.getErrorType());

            // Transform Element
            Element transform = new Element("transform", EE_NAMESPACE.getNamespace());
            transform.addNamespaceDeclaration(EE_NAMESPACE.getNamespace());
            transform.setAttribute("schemaLocation", EE_NAMESPACE.getNamespace().getURI() + " " + EE_NAMESPACE.getLocation(), XSI_NAMESPACE.getNamespace());

            // Set Payload and Variable Element
            Element setPayload = new Element("set-payload", EE_NAMESPACE.getNamespace());
            CDATA cDataSection = new CDATA(getTransformText(statusCodeMapping.getMessage()));

            Element statusCodeVariable = new Element("set-variable", EE_NAMESPACE.getNamespace());
            statusCodeVariable.setAttribute("variableName", "httpStatus");

            setPayload.addContent(cDataSection);
            statusCodeVariable.addContent(statusCodeMapping.getStatusCode());
            transform.addContent(setPayload);
            transform.addContent(statusCodeVariable);
            errorMapping.addContent(transform);
            errorHandler.addContent(errorMapping);

        }
    }

    @Override
    public Element generate() {
        return errorHandler;
    }

    private String getTransformText(String message) {
        return "\n             %output application/json\n" +
                "             ---\n" +
                "             {message: \"" + message + "\"\n";
    }

    public static class StatusCodeMapping {

        private final int statusCode;
        private final String errorType;
        private final String message;

        public StatusCodeMapping(int statusCode, String errorType, String message) {
            this.statusCode = statusCode;
            this.errorType = errorType;
            this.message = message;
        }

        public String getStatusCode() {
            return Integer.toString(statusCode);
        }

        public String getErrorType() {
            return errorType;
        }

        public String getMessage() {
            return message;
        }
    }
}
