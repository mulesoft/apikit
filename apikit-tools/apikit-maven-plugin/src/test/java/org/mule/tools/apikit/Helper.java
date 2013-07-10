package org.mule.tools.apikit;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class Helper {
    public static void testEqualsHelper(Method a, Method b, Method f) throws Exception {
        Object o = new Object();
        Object api = f.invoke(o, a.invoke(o), b.invoke(o));
        Object api2 = f.invoke(o, a.invoke(o), b.invoke(o));

        assertEquals(api, api2);

        Object api3 = f.invoke(o, b.invoke(o), b.invoke(o));
        Object api4 = f.invoke(o, a.invoke(o), b.invoke(o));

        assertFalse(api3.equals(api4));

        Object api5 = f.invoke(o, a.invoke(o), b.invoke(o));
        Object api6 = f.invoke(o, a.invoke(o), b.invoke(o));

        Set<Object> apis = new HashSet<Object>();
        apis.add(api5);
        apis.add(api6);

        assertEquals(1, apis.size());
    }


    public static String nonSpaceOutput(Element element) {
        XMLOutputter xout = new XMLOutputter(Format.getCompactFormat());
        return xout.outputString(element);
    }

    public static String nonSpaceOutput(Document doc) {
        XMLOutputter xout = new XMLOutputter(Format.getCompactFormat());
        return xout.outputString(doc.getRootElement().getChildren());
    }
}
