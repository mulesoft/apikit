/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import static org.mule.runtime.api.meta.Category.COMMUNITY;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.metadata.api.ClassTypeLoader;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.meta.model.XmlDslModel;
import org.mule.runtime.api.meta.model.declaration.fluent.ConfigurationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.OperationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclarer;
import org.mule.runtime.api.meta.model.error.ErrorModel;
import org.mule.runtime.api.meta.model.error.ErrorModelBuilder;
import org.mule.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;

public class ApikitExtensionDeclarer
{
    public static final String EXTENSION_NAME = "http";
    public static final String EXTENSION_DESCRIPTION = "Http Connector";
    public static final String VENDOR = "Mulesoft";
    public static final String VERSION = "1.0";
    public static final MuleVersion MIN_MULE_VERSION = new MuleVersion("4.0");

    protected final BaseTypeBuilder typeBuilder = BaseTypeBuilder.create(JAVA);

    public ExtensionDeclarer generateDeclarer()
    {
        ErrorModel muleAnyErrorType = ErrorModelBuilder.newError("ANY", "MULE").build();//TODO CHECK IF THIS ERROR TYPE HAS TO BE DECLARED AT EXTENSION LEVEL
        ErrorModel apikitAnyErrorType = ErrorModelBuilder.newError("ANY", "APIKIT").withParent(muleAnyErrorType).build();
        ErrorModel badRequestErrorModel = ErrorModelBuilder.newError("BAD_REQUEST", "APIKIT").withParent(apikitAnyErrorType).build();
        ErrorModel notAcceptableErrorModel = ErrorModelBuilder.newError("NOT_ACCEPTABLE", "APIKIT").withParent(apikitAnyErrorType).build();
        ErrorModel unsupportedMediaTypeErrorModel = ErrorModelBuilder.newError("UNSUPPORTED_MEDIA_TYPE", "APIKIT").withParent(apikitAnyErrorType).build();
        ErrorModel methodNotAllowedErrorModel = ErrorModelBuilder.newError("METHOD_NOT_ALLOWED", "APIKIT").withParent(apikitAnyErrorType).build();
        ErrorModel notFoundErrorModel = ErrorModelBuilder.newError("NOT_FOUND", "APIKIT").withParent(apikitAnyErrorType).build();

        ExtensionDeclarer extensionDeclarer = new ExtensionDeclarer();
        extensionDeclarer.named(EXTENSION_NAME)
                .describedAs(EXTENSION_DESCRIPTION)
                .fromVendor(VENDOR)
                .onVersion(VERSION)
                .withCategory(COMMUNITY)
                .withMinMuleVersion(MIN_MULE_VERSION)
                .withXmlDsl(XmlDslModel.builder().build())
                .withErrorModel(badRequestErrorModel)
                .withErrorModel(apikitAnyErrorType)
                .withErrorModel(notAcceptableErrorModel)
                .withErrorModel(unsupportedMediaTypeErrorModel)
                .withErrorModel(methodNotAllowedErrorModel)
                .withErrorModel(notFoundErrorModel);

        ClassTypeLoader typeLoader = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();

        //config
        ConfigurationDeclarer apikitConfig = extensionDeclarer.withConfig("config")
                .describedAs("apikit");
        ParameterGroupDeclarer parameterGroupDeclarer = apikitConfig.onDefaultParameterGroup();
        parameterGroupDeclarer.withRequiredParameter("raml").ofType(typeLoader.load(String.class));
        parameterGroupDeclarer.withRequiredParameter("outboundHeadersMapName").ofType(typeLoader.load(String.class));
        parameterGroupDeclarer.withRequiredParameter("httpStatusVarName").ofType(typeLoader.load(String.class));
        parameterGroupDeclarer.withOptionalParameter("flowMappings").ofType(typeBuilder.arrayType().of(typeLoader.load(FlowMapping.class)).build());

        //router
        OperationDeclarer routerDeclarer = apikitConfig.withOperation("router");
        routerDeclarer.withOutputAttributes().ofType(typeLoader.load(HttpRequestAttributes.class));
        routerDeclarer.withOutput().ofType(typeLoader.load(Object.class));
        routerDeclarer.withError(badRequestErrorModel)
            .withError(notAcceptableErrorModel)
            .withError(unsupportedMediaTypeErrorModel)
            .withError(methodNotAllowedErrorModel)
            .withError(notFoundErrorModel);

        //console
        OperationDeclarer consoleDeclarer = apikitConfig.withOperation("console");
        consoleDeclarer.withOutputAttributes().ofType(typeLoader.load(HttpRequestAttributes.class));
        consoleDeclarer.withOutput().ofType(typeLoader.load(Object.class));
        consoleDeclarer.withError(notFoundErrorModel);

        return extensionDeclarer;
    }
}
