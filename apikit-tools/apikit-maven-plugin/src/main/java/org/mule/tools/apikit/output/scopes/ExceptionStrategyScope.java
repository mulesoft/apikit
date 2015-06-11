/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

import org.mule.tools.apikit.misc.APIKitTools;

import java.util.Arrays;
import java.util.List;

import org.jdom2.Element;

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

    public ExceptionStrategyScope(String apiId) {
        List<StatusCodeMapping> statusCodeMappings = Arrays.asList(
                new StatusCodeMapping(404, "org.mule.module.apikit.exception.NotFoundException", "{ \"message\": \"Resource not found\" }"),
                new StatusCodeMapping(405, "org.mule.module.apikit.exception.MethodNotAllowedException", "{ \"message\": \"Method not allowed\" }"),
                new StatusCodeMapping(415, "org.mule.module.apikit.exception.UnsupportedMediaTypeException", "{ \"message\": \"Unsupported media type\" }"),
                new StatusCodeMapping(406, "org.mule.module.apikit.exception.NotAcceptableException", "{ \"message\": \"Not acceptable\" }"),
                new StatusCodeMapping(400, "org.mule.module.apikit.exception.BadRequestException", "{ \"message\": \"Bad request\" }")
        );

        exceptionStrategy = new Element("mapping-exception-strategy",
                APIKitTools.API_KIT_NAMESPACE.getNamespace());

        exceptionStrategy.setAttribute("name", apiId + "-" + "apiKitGlobalExceptionMapping");

        for (StatusCodeMapping statusCodeMapping : statusCodeMappings) {
            Element mapping = new Element("mapping",
                    APIKitTools.API_KIT_NAMESPACE.getNamespace());

            mapping.setAttribute("statusCode", statusCodeMapping.getStatusCode());

            Element exception = new Element("exception", APIKitTools.API_KIT_NAMESPACE.getNamespace());
            exception.setAttribute("value", statusCodeMapping.getException());
            mapping.addContent(exception);
            
            Element setContentType = new Element("set-property", XMLNS_NAMESPACE.getNamespace());
            setContentType.setAttribute("propertyName", "Content-Type");
            setContentType.setAttribute("value", "application/json");
            mapping.addContent(setContentType);

            Element setPayload = new Element("set-payload", XMLNS_NAMESPACE.getNamespace());
            setPayload.setAttribute("value", statusCodeMapping.getMsg());
            mapping.addContent(setPayload);

            exceptionStrategy.addContent(mapping);
        }
    }

    @Override
    public Element generate() {
        return exceptionStrategy;
    }
}
