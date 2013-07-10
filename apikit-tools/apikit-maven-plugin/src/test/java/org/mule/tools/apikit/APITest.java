package org.mule.tools.apikit;

import org.junit.Test;
import org.mule.tools.apikit.model.API;

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

    public static API createAPIBinding(File a, File b) {
        return API.createAPIBinding(a,b,"/api");
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
