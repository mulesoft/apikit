/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.common.test;

import org.junit.Assert;
import org.junit.Test;
import org.mule.raml.interfaces.common.APISyncUtils;

public class APISyncUtilsTest {

  private static final String ORG_ID = "org_id";
  private static final String ARTIFACT_ID = "artifact_id";
  private static final String VERSION = "1.0.0";
  private static final String FOLDER = "folder";
  private static final String FILE_RAML = "file.raml";
  private static final String EXCHANGE_MODULES = "exchange_modules/";
  private static final String GAV = ORG_ID + "/" + ARTIFACT_ID + "/" + VERSION;
  private static final String EXCHANGE_MODULES_GAV = EXCHANGE_MODULES + GAV;
  private static final String TEST_RESOURCE_WITH_FOLDER = EXCHANGE_MODULES_GAV + "/" + FOLDER + "/" + FILE_RAML;
  private static final String TEST_RESOURCE_WITH_FOLDER_TWICE =
      EXCHANGE_MODULES_GAV + "/" + FOLDER + "/" + FOLDER + "/" + FILE_RAML;
  private static final String TEST_RESOURCE_WITHOUT_FOLDER = EXCHANGE_MODULES_GAV + "/" + FILE_RAML;
  private static final String RESOURCE_GAV = "resource::" + ORG_ID + ":" + ARTIFACT_ID + ":" + VERSION + ":raml-fragment:zip:";
  private static final String RESOURCE_ORG_ID_ARTIFACT_ID_1_0_0_RAML_FRAGMENT_ZIP_FOLDER_FILE_RAML =
      RESOURCE_GAV + FOLDER + "/" + FILE_RAML;
  private static final String RESOURCE_ORG_ID_ARTIFACT_ID_1_0_0_RAML_FRAGMENT_ZIP_FOLDER_TWICE_FILE_RAML =
      RESOURCE_GAV + FOLDER + "/" + FOLDER + "/" + FILE_RAML;
  private static final String RESOURCE_ORG_ID_ARTIFACT_ID_1_0_0_RAML_FRAGMENT_ZIP_WITHOUT_FILE_RAMLL = RESOURCE_GAV + FILE_RAML;
  private static final String RESOURCE_STRING_WITHOUT_FILE_PATH_SHOULD_NOT_MATCH =
      "Resource string without file path should not match";
  private static final String RESOURCE_STRING_IS_NOT_CORRECTLY_FORMED = "Resource string is not correctly formed";
  private static final String RESOURCE_STRING_WITHOUT_EXCHANGE_MODULES_SHOULD_NOT_MATCH =
      "Resource string without exchange_modules should not match";
  private static final String NOT_EXCHANGE_MODULES = "not_exchange_modules";


  @Test
  public void testAPISyncResourceStringWithIncludesInsideSubfolders() {
    String apiSyncResourceString = APISyncUtils.toApiSyncResource(TEST_RESOURCE_WITH_FOLDER);
    Assert.assertEquals(RESOURCE_STRING_IS_NOT_CORRECTLY_FORMED,
                        RESOURCE_ORG_ID_ARTIFACT_ID_1_0_0_RAML_FRAGMENT_ZIP_FOLDER_FILE_RAML, apiSyncResourceString);
  }

  @Test
  public void testAPISyncResourceStringWithIncludesInsideSubfoldersInsideSubfolder() {
    String apiSyncResourceString = APISyncUtils.toApiSyncResource(TEST_RESOURCE_WITH_FOLDER_TWICE);
    Assert.assertEquals(RESOURCE_STRING_IS_NOT_CORRECTLY_FORMED,
                        RESOURCE_ORG_ID_ARTIFACT_ID_1_0_0_RAML_FRAGMENT_ZIP_FOLDER_TWICE_FILE_RAML, apiSyncResourceString);
  }

  @Test
  public void testAPISyncResourceStringWithoutFolders() {
    String apiSyncResourceString = APISyncUtils.toApiSyncResource(TEST_RESOURCE_WITHOUT_FOLDER);
    Assert.assertEquals(RESOURCE_STRING_IS_NOT_CORRECTLY_FORMED,
                        RESOURCE_ORG_ID_ARTIFACT_ID_1_0_0_RAML_FRAGMENT_ZIP_WITHOUT_FILE_RAMLL, apiSyncResourceString);
  }

  @Test
  public void testApiSyncResourceWithoutPathAtTheEndShouldNotMatch() {
    String apiSyncResourceString = APISyncUtils.toApiSyncResource(EXCHANGE_MODULES_GAV);
    Assert.assertNull(RESOURCE_STRING_WITHOUT_FILE_PATH_SHOULD_NOT_MATCH, apiSyncResourceString);
  }

  @Test
  public void testApiSyncResourceWithoutExchangeModulesAtTheStartShouldNotMatch() {
    String apiSyncResourceString = APISyncUtils.toApiSyncResource(NOT_EXCHANGE_MODULES + GAV);
    Assert.assertNull(RESOURCE_STRING_WITHOUT_EXCHANGE_MODULES_SHOULD_NOT_MATCH, apiSyncResourceString);
  }

}
