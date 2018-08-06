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
