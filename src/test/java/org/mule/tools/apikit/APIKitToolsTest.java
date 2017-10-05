/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.junit.Assert;
import org.junit.Test;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;

public class APIKitToolsTest {

  @Test
  public void getPartsFromUris() {
    String uri = "http://localhost";
    Assert.assertEquals("localhost", APIKitTools.getHostFromUri(uri));
    Assert.assertEquals(Integer.toString(API.DEFAULT_PORT), APIKitTools.getPortFromUri(uri));
    Assert.assertEquals("/*", APIKitTools.getPathFromUri(uri, true));
    Assert.assertEquals("/", APIKitTools.getPathFromUri(uri, false));

    uri = "http://localhost.com:333/path/path2/";
    Assert.assertEquals("localhost.com", APIKitTools.getHostFromUri(uri));
    Assert.assertEquals("333", APIKitTools.getPortFromUri(uri));
    Assert.assertEquals("/path/path2/*", APIKitTools.getPathFromUri(uri, true));
    Assert.assertEquals("/path/path2/", APIKitTools.getPathFromUri(uri, false));

    uri = "http://localhost.com:${port}/path/path2/";
    Assert.assertEquals("localhost.com", APIKitTools.getHostFromUri(uri));
    Assert.assertEquals("${port}", APIKitTools.getPortFromUri(uri));
    Assert.assertEquals("/path/path2/*", APIKitTools.getPathFromUri(uri, true));
    Assert.assertEquals("/path/path2/", APIKitTools.getPathFromUri(uri, false));

  }

  @Test
  public void isInboundEndpointDefault() {
    Assert.assertTrue(APIKitTools.defaultIsInboundEndpoint("3.5.0"));
    Assert.assertFalse(APIKitTools.defaultIsInboundEndpoint("3.6.0"));
    Assert.assertFalse(APIKitTools.defaultIsInboundEndpoint("3.7.0"));
    Assert.assertFalse(APIKitTools.defaultIsInboundEndpoint("4.0.0"));
    Assert.assertFalse(APIKitTools.defaultIsInboundEndpoint("invalid"));
    Assert.assertFalse(APIKitTools.defaultIsInboundEndpoint(""));
    Assert.assertFalse(APIKitTools.defaultIsInboundEndpoint(null));
  }

  @Test
  public void canExtensionsBeEnabled() throws NoSuchFieldException, IllegalAccessException {
    Assert.assertTrue(APIKitTools.canExtensionsBeEnabled("4.0.0"));
    Assert.assertTrue(APIKitTools.canExtensionsBeEnabled("3.7.3"));
    Assert.assertTrue(APIKitTools.canExtensionsBeEnabled("3.7.3-SNAPSHOT"));
    Assert.assertTrue(APIKitTools.canExtensionsBeEnabled("3.7.0"));
    Assert.assertFalse(APIKitTools.canExtensionsBeEnabled("3.6.0"));
    Assert.assertFalse(APIKitTools.canExtensionsBeEnabled("3.5.0"));
    Assert.assertFalse(APIKitTools.canExtensionsBeEnabled("invalid"));
    Assert.assertFalse(APIKitTools.canExtensionsBeEnabled(""));
    Assert.assertFalse(APIKitTools.canExtensionsBeEnabled(null));
  }

}
