/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.junit.Test;
import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.interfaces.Notifier;
import org.mule.module.apikit.metadata.interfaces.ResourceLoader;
import org.mule.module.apikit.metadata.utils.MockedApplicationModel;
import org.mule.runtime.config.spring.api.dsl.model.ApplicationModel;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class MetadataModuleTestCase {

  @Test
  public void testBasicMetadataModule() throws Exception {

    ResourceLoader resourceLoader = new TestResourceLoader();
    Notifier notifier = new TestNotifier();

    ApplicationModel applicationModel = createApplicationModel("org/mule/module/apikit/metadata/flow-mappings/app.xml");
    assertThat(applicationModel, notNullValue());

    Metadata metadata = new Metadata.Builder()
        .withApplicationModel(applicationModel)
        .withResourceLoader(resourceLoader)
        .withNotifier(notifier)
        .build();

    Optional<FunctionType> createNewBookFlow = metadata.getMetadataForFlow("createNewBook");
    Optional<FunctionType> getAllBooks = metadata.getMetadataForFlow("get:\\books:router-config");
    Optional<FunctionType> flowMappingDoesNotExist = metadata.getMetadataForFlow("flowMappingDoesNotExist");
    Optional<FunctionType> petshopApiGetCustomers = metadata.getMetadataForFlow("get:\\customers\\pets:petshop-api");
    Optional<FunctionType> petShopApiCreateCustomer = metadata.getMetadataForFlow("post:\\customers:petshop-api");

    assertThat(createNewBookFlow.isPresent(), is(true));
    assertThat(getAllBooks.isPresent(), is(true));
    assertThat(flowMappingDoesNotExist.isPresent(), is(false));
    assertThat(petShopApiCreateCustomer.isPresent(), is(true));
    assertThat(petshopApiGetCustomers.isPresent(), is(true));
  }


  @Test
  public void singleApiWithFlowsWithoutConfigRef() throws Exception {

    ResourceLoader resourceLoader = new TestResourceLoader();
    Notifier notifier = new TestNotifier();

    ApplicationModel applicationModel =
        createApplicationModel("org/mule/module/apikit/metadata/single-api-with-no-name/mule-config.xml");
    assertThat(applicationModel, notNullValue());

    Metadata metadata = new Metadata.Builder()
        .withApplicationModel(applicationModel)
        .withResourceLoader(resourceLoader)
        .withNotifier(notifier)
        .build();

    Optional<FunctionType> getAllCustomersPets = metadata.getMetadataForFlow("get:\\customers\\pets");
    Optional<FunctionType> createCustomer = metadata.getMetadataForFlow("post:\\customers");

    assertThat(getAllCustomersPets.isPresent(), is(true));
    assertThat(createCustomer.isPresent(), is(true));
  }

  @Test
  public void ramlApplicationInRaml08() throws Exception {

    ResourceLoader resourceLoader = new TestResourceLoader();
    Notifier notifier = new TestNotifier();

    ApplicationModel applicationModel = createApplicationModel("org/mule/module/apikit/metadata/api-in-raml08/mule-config.xml");
    assertThat(applicationModel, notNullValue());

    Metadata metadata = new Metadata.Builder()
        .withApplicationModel(applicationModel)
        .withResourceLoader(resourceLoader)
        .withNotifier(notifier)
        .build();

    Optional<FunctionType> putResources = metadata.getMetadataForFlow("put:\\resources:application\\json:router-config");
    Optional<FunctionType> getResources = metadata.getMetadataForFlow("get:\\resources:router-config");
    Optional<FunctionType> postResources = metadata.getMetadataForFlow("post:\\resources:router-config");
    Optional<FunctionType> postUrlEncoded =
        metadata.getMetadataForFlow("post:\\url-encoded:application\\x-www-form-urlencoded:router-config");
    Optional<FunctionType> postMultipart = metadata.getMetadataForFlow("post:\\multipart:multipart\\form-data:router-config");

    assertThat(putResources.isPresent(), is(true));
    assertThat(getResources.isPresent(), is(true));
    assertThat(postResources.isPresent(), is(true));
    assertThat(postUrlEncoded.isPresent(), is(true));
    assertThat(postMultipart.isPresent(), is(true));
  }

  private ApplicationModel createApplicationModel(String resourceName) throws Exception {
    final MockedApplicationModel.Builder builder = new MockedApplicationModel.Builder();
    builder.addConfig("apiKitSample", getClass().getClassLoader().getResourceAsStream(resourceName));
    final MockedApplicationModel mockedApplicationModel = builder.build();
    return mockedApplicationModel.getApplicationModel();
  }

}
