/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import org.junit.Assert;
import org.junit.Test;
import org.mule.module.apikit.CharsetUtils;

public class CharsetUtilsTestCase {
    @Test public void headerCharset() {
        String charset = CharsetUtils.getCharset("application/json; charset=windows-1252; skipnullon=\"everywhere\"");
        Assert.assertEquals(charset, "windows-1252");
    }
}
