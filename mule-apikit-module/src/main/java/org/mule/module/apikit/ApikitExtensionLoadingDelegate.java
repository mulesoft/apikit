/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.metadata.api.ClassTypeLoader;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.ObjectType;
import org.mule.module.apikit.api.Parser;
import org.mule.runtime.api.meta.model.ImportedTypeModel;
import org.mule.runtime.api.meta.model.XmlDslModel;
import org.mule.runtime.api.meta.model.declaration.fluent.ConfigurationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.OperationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclarer;
import org.mule.runtime.api.meta.model.error.ErrorModel;
import org.mule.runtime.api.meta.model.error.ErrorModelBuilder;
import org.mule.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.api.loader.ExtensionLoadingDelegate;

import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import static org.mule.module.apikit.api.Parser.AUTO;
import static org.mule.runtime.api.meta.Category.COMMUNITY;

public class ApikitExtensionLoadingDelegate implements ExtensionLoadingDelegate {

  public static final String EXTENSION_NAME = "APIKit";
  public static final String PREFIX_NAME = "apikit";
  public static final String EXTENSION_DESCRIPTION = "APIKit plugin";
  public static final String VENDOR = "Mulesoft";
  public static final String VERSION = "1.2.0-SNAPSHOT";
  public static final String XSD_FILE_NAME = "mule-apikit.xsd";
  private static final String UNESCAPED_LOCATION_PREFIX = "http://";
  private static final String SCHEMA_LOCATION = "www.mulesoft.org/schema/mule/mule-apikit";
  private static final String SCHEMA_VERSION = "current";

  protected final BaseTypeBuilder typeBuilder = BaseTypeBuilder.create(JAVA);

  @Override
  public void accept(ExtensionDeclarer extensionDeclarer, ExtensionLoadingContext extensionLoadingContext) {
    ErrorModel muleAnyErrorType = ErrorModelBuilder.newError("ANY", "MULE").build();
    ErrorModel apikitAnyErrorType = ErrorModelBuilder.newError("ANY", "APIKIT").withParent(muleAnyErrorType).build();
    ErrorModel badRequestErrorModel = ErrorModelBuilder.newError("BAD_REQUEST", "APIKIT").withParent(apikitAnyErrorType).build();
    ErrorModel notAcceptableErrorModel =
        ErrorModelBuilder.newError("NOT_ACCEPTABLE", "APIKIT").withParent(apikitAnyErrorType).build();
    ErrorModel unsupportedMediaTypeErrorModel =
        ErrorModelBuilder.newError("UNSUPPORTED_MEDIA_TYPE", "APIKIT").withParent(apikitAnyErrorType).build();
    ErrorModel methodNotAllowedErrorModel =
        ErrorModelBuilder.newError("METHOD_NOT_ALLOWED", "APIKIT").withParent(apikitAnyErrorType).build();
    ErrorModel notFoundErrorModel = ErrorModelBuilder.newError("NOT_FOUND", "APIKIT").withParent(apikitAnyErrorType).build();
    ErrorModel notImplementedErrorModel =
        ErrorModelBuilder.newError("NOT_IMPLEMENTED", "APIKIT").withParent(apikitAnyErrorType).build();

    XmlDslModel xmlDslModel = XmlDslModel.builder()
        .setPrefix(PREFIX_NAME)
        .setXsdFileName(XSD_FILE_NAME)
        .setSchemaVersion(VERSION)
        .setSchemaLocation(String.format("%s/%s/%s", UNESCAPED_LOCATION_PREFIX + SCHEMA_LOCATION, SCHEMA_VERSION, XSD_FILE_NAME))
        .setNamespace(UNESCAPED_LOCATION_PREFIX + SCHEMA_LOCATION)
        .build();
    ClassTypeLoader typeLoader = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();

    extensionDeclarer.named(EXTENSION_NAME)
        .describedAs(EXTENSION_DESCRIPTION)
        .fromVendor(VENDOR)
        .onVersion(VERSION)
        .withCategory(COMMUNITY)
        .withXmlDsl(xmlDslModel)
        .withErrorModel(badRequestErrorModel)
        .withErrorModel(apikitAnyErrorType)
        .withErrorModel(notAcceptableErrorModel)
        .withErrorModel(unsupportedMediaTypeErrorModel)
        .withErrorModel(methodNotAllowedErrorModel)
        .withErrorModel(notFoundErrorModel)
        .withErrorModel(notImplementedErrorModel);
    extensionDeclarer.withImportedType(new ImportedTypeModel((ObjectType) typeLoader.load(HttpRequestAttributes.class)));

    //config
    ConfigurationDeclarer apikitConfig = extensionDeclarer.withConfig("config")
        .describedAs(PREFIX_NAME);
    ParameterGroupDeclarer parameterGroupDeclarer = apikitConfig.onDefaultParameterGroup();
    parameterGroupDeclarer.withRequiredParameter("raml").ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withRequiredParameter("outboundHeadersMapName").ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withRequiredParameter("httpStatusVarName").ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("keepRamlBaseUri").defaultingTo(false).ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("disableValidations").defaultingTo(false).ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("queryParamsStrictValidation").defaultingTo(false)
        .ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("headersStrictValidation").defaultingTo(false)
        .ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("parser").defaultingTo(AUTO).ofType(typeLoader.load(Parser.class));
    parameterGroupDeclarer.withOptionalParameter("flowMappings")
        .ofType(typeBuilder.arrayType().of(typeLoader.load(FlowMapping.class)).build());

    //router
    OperationDeclarer routerDeclarer = apikitConfig.withOperation("router");
    routerDeclarer.withOutputAttributes().ofType(typeLoader.load(HttpRequestAttributes.class));
    routerDeclarer.withOutput().ofType(typeLoader.load(Object.class));
    routerDeclarer.withErrorModel(badRequestErrorModel)
        .withErrorModel(notAcceptableErrorModel)
        .withErrorModel(unsupportedMediaTypeErrorModel)
        .withErrorModel(methodNotAllowedErrorModel)
        .withErrorModel(notFoundErrorModel);

    //console
    OperationDeclarer consoleDeclarer = apikitConfig.withOperation("console");
    consoleDeclarer.withOutputAttributes().ofType(typeLoader.load(HttpRequestAttributes.class));
    consoleDeclarer.withOutput().ofType(typeLoader.load(Object.class));
    consoleDeclarer.withErrorModel(notFoundErrorModel);

  }
}
