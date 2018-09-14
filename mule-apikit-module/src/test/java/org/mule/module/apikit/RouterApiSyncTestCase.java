/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.jayway.restassured.RestAssured;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import java.io.File;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mule.runtime.deployment.model.api.application.ApplicationDescriptor.MULE_APPLICATION_CLASSIFIER;
import static org.mule.test.infrastructure.maven.MavenTestUtils.installMavenArtifact;

@ArtifactClassLoaderRunnerConfig
@Ignore
public class RouterApiSyncTestCase extends MuleArtifactFunctionalTestCase {

  public static final String RAML_LIBRARY = "raml-library";
  public static final String RAML_API = "raml-api";
  private static final BundleDescriptor ramlLibraryDescriptor = new BundleDescriptor.Builder().setGroupId("com.mycompany")
      .setArtifactId(RAML_LIBRARY).setVersion("1.1.0").setClassifier("raml-fragment").setType("zip").build();
  private static final BundleDescriptor ramlApiDescriptor = new BundleDescriptor.Builder().setGroupId("com.mycompany")
      .setArtifactId(RAML_API).setVersion("1.0.0").setClassifier("raml").setType("zip").build();

  private static final File ramlFragmentArtifact = installMavenArtifact(RAML_LIBRARY, ramlLibraryDescriptor);
  private static final File ramlApiArtifact = installMavenArtifact(RAML_API, ramlApiDescriptor);



  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  public int getTestTimeoutSecs() {
    return 6000;
  }

  @Override
  protected void doSetUp() throws Exception {
    RestAssured.port = serverPort.getNumber();
    super.doSetUp();
  }

  @Override
  protected String getConfigResources() {
    return "org/mule/module/apikit/simple-routing/api-sync.xml";
  }


  @Test
  public void simpleRouting() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(is("hello"))
        .statusCode(200)
        .when().get("/api/resources");
  }


}
