/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.jayway.restassured.RestAssured;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.infrastructure.process.rules.MuleDeployment;

import java.io.File;

import static org.mule.module.apikit.helpers.AbstractEEAppControl.builderWithDefaultConfig;
import static com.jayway.restassured.RestAssured.given;
import static org.mule.runtime.deployment.model.api.application.ApplicationDescriptor.MULE_APPLICATION_CLASSIFIER;
import static org.mule.test.infrastructure.maven.MavenTestUtils.installMavenArtifact;

@Ignore
public class RouterApiSyncTestCase extends AbstractMuleTestCase {

  private static final String APPLICATION = "resource-classloading";
  public static final String RAML_LIBRARY = "raml-library";
  public static final String RAML_API = "raml-api";
  private static final BundleDescriptor ramlLibraryDescriptor = new BundleDescriptor.Builder().setGroupId("com.mycompany")
      .setArtifactId(RAML_LIBRARY).setVersion("1.1.0").setClassifier("raml-fragment").setType("zip").build();
  private static final BundleDescriptor ramlApiDescriptor = new BundleDescriptor.Builder().setGroupId("com.mycompany")
      .setArtifactId(RAML_API).setVersion("1.0.0").setClassifier("raml").setType("zip").build();
  private static final BundleDescriptor applicationDescriptor = new BundleDescriptor.Builder().setGroupId("test")
      .setArtifactId(APPLICATION).setVersion("1.0.0").setClassifier(MULE_APPLICATION_CLASSIFIER).build();
  private static final File ramlFragmentArtifact = installMavenArtifact(RAML_LIBRARY, ramlLibraryDescriptor);
  private static final File ramlApiArtifact = installMavenArtifact(RAML_API, ramlApiDescriptor);
  private static final File applicationArtifact = installMavenArtifact(APPLICATION, applicationDescriptor);



  private static final int DEPLOY_TIMEOUT = 120;
  private static DynamicPort dynamicPort = new DynamicPort("serverPort");
  private static final String HTTP_PORT = dynamicPort.getValue();


  @ClassRule
  public static MuleDeployment standalone = builderWithDefaultConfig()
      .withApplications(applicationArtifact.getAbsolutePath())
      .withProperty("-M-Dhttp.port", HTTP_PORT)
      .timeout(DEPLOY_TIMEOUT).deploy();

  @Before
  public void attachProperties() {
    standalone.attachProperties();
    RestAssured.port = dynamicPort.getNumber();
  }

  @After
  public void attachLogs() {
    standalone.attachServerLog();
    standalone.attachAppLog(applicationDescriptor.getArtifactFileName());
  }


  @Test
  public void simpleRouting() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().statusCode(200)
        .when().get("/api/test");
  }

}
