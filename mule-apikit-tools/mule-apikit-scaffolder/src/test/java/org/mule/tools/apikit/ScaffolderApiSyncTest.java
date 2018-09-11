/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mule.tools.apikit.misc.FileListUtils;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ScaffolderApiSyncTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private FileListUtils fileListUtils = new FileListUtils();

  @Test
  public void testSimpleGeneration() throws IOException {
      ScaffolderResourceLoader scaffolderResourceLoaderMock = Mockito.mock(ScaffolderResourceLoader.class);
      Mockito.doReturn(getInputStream("src/test/resources/scaffolder/simple.raml")).when(scaffolderResourceLoaderMock).getResourceAsStream("test");


      Assert.assertEquals( IOUtils.toString( scaffolderResourceLoaderMock.getResourceAsStream("test")),IOUtils.toString( getInputStream("src/test/resources/scaffolder/simple.raml") ));
  }

  private  InputStream getInputStream(String resourcePath){
      InputStream resourceStream = null;

      try {
          resourceStream = new FileInputStream(resourcePath);
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      }

      return resourceStream;
  }

}
