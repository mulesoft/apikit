/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;
import org.mule.tools.apikit.Helper;
import org.mule.tools.apikit.output.GenerationModel;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class APIKitFlowScopeTest {
    @Test
    public void testGenerate() throws Exception {
        GenerationModel flowEntry = mock(GenerationModel.class);
        when(flowEntry.getFlowName()).thenReturn("get:/pet");
        when(flowEntry.getName()).thenReturn("retrievePet");
        when(flowEntry.getExample()).thenReturn("Hello world!");

        Document doc = new Document();
        Element mule = new Element("mule");

        mule.addContent(new APIKitFlowScope(flowEntry).generate());
        doc.setContent(mule);

        String s = Helper.nonSpaceOutput(doc);

        Diff diff = XMLUnit.compareXML("<flow " +
                "xmlns='http://www.mulesoft.org/schema/mule/core' " +
                "name='get:/pet'><set-payload " +
                "value='Hello world!' /></flow>", s);

        assertTrue(diff.toString(), diff.similar());
    }
}
