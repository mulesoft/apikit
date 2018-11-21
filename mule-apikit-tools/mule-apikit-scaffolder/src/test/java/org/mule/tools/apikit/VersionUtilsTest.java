/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.junit.Test;
import static org.mule.tools.apikit.misc.VersionUtils.getMaxVersion;
import static org.junit.Assert.assertEquals;

public class VersionUtilsTest {

  private static final String minVersion = "4.1.4";

  @Test
  public void versionUtilsTests() {

    String version;

    version = getMaxVersion("4.0.0", minVersion);
    assertEquals(minVersion, version);

    version = getMaxVersion(null, minVersion);
    assertEquals(minVersion, version);

    version = getMaxVersion("Not Valid Version", minVersion);
    assertEquals(minVersion, version);

    version = getMaxVersion("4.2", minVersion);
    assertEquals(minVersion, version);

    final String greaterVersion = "4.1.5";

    version = getMaxVersion(greaterVersion, minVersion);
    assertEquals(greaterVersion, version);


  }
}
