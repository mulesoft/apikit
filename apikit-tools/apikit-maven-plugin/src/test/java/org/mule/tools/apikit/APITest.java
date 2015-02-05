/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.junit.Test;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;

import java.io.File;
import static org.mule.tools.apikit.Helper.testEqualsHelper;

public class APITest {

    public static File createFileA() {
        return new File("a");
    }

    public static File createFileB() {
        return new File("b");
    }

    private static File file = new File("a");

    public static File createSameFile() {
        return file;
    }

    public static API createAPIBinding(File a, File b)
   {
       return new APIFactory().createAPIBinding(a, b, "http://localhost:80", "/api/*", null);
    }

    @Test
    public void testEquals() throws Exception {
        testEqualsHelper(APITest.class.getMethod("createFileA"),
                APITest.class.getMethod("createFileB"),
                APITest.class.getMethod("createAPIBinding", File.class, File.class)
                );
        testEqualsHelper(APITest.class.getMethod("createSameFile"),
                APITest.class.getMethod("createFileB"),
                APITest.class.getMethod("createAPIBinding", File.class, File.class)
        );
    }

}
