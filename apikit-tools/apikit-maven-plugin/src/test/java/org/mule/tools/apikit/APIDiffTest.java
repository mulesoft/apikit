/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionPair;
import org.mule.tools.apikit.input.APIDiff;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class APIDiffTest {

    private APIFactory apiFactory;

    @Before
    public void setUp() {
        this.apiFactory = new APIFactory();
    }

    private Set<ResourceActionPair> computeDifferenceSetHelper(HashSet<ResourceActionPair> a, HashSet<ResourceActionPair> b) {
        RAMLFilesParser RAMLFilesParser = mock(RAMLFilesParser.class);
        when(RAMLFilesParser.getEntries()).thenReturn(a);

        MuleConfigParser muleConfigParser = mock(MuleConfigParser.class);
        when(muleConfigParser.getEntries()).thenReturn(b);

        return new APIDiff(RAMLFilesParser, muleConfigParser).getEntries();

    }

    @Test
    public void testComputeDifferenceEmpty() throws Exception {
        HashSet<ResourceActionPair> a = new HashSet<ResourceActionPair>();
        HashSet<ResourceActionPair> b = new HashSet<ResourceActionPair>();

        Set<ResourceActionPair> heavenFlowEntries = computeDifferenceSetHelper(a, b);

        assertNotNull(heavenFlowEntries);
        assertTrue(heavenFlowEntries.isEmpty());
    }

    @Test
    public void testComputeDifference() throws Exception {
        API fromYAMLFile = apiFactory.createAPIBinding(new File("sample.yaml"), null, "https://localhost/api");

        HashSet<ResourceActionPair> a = new HashSet<ResourceActionPair>();
        ResourceActionPair fab = new ResourceActionPair(fromYAMLFile, "a", "b");
        a.add(fab);

        HashSet<ResourceActionPair> b = new HashSet<ResourceActionPair>();
        ResourceActionPair feb = new ResourceActionPair(fromYAMLFile, "a", "b");
        b.add(feb);


        Set<ResourceActionPair> heavenFlowEntries = computeDifferenceSetHelper(a, b);

        assertEquals(feb, fab);
        assertNotNull(heavenFlowEntries);
        assertEquals(0, heavenFlowEntries.size());
    }

    @Test
    public void testComputeDifferenceMismatching() throws Exception {
        API fromYAMLFile = apiFactory.createAPIBinding(new File("sample.yaml"), null, "https://localhost/api");

        HashSet<ResourceActionPair> a = new HashSet<ResourceActionPair>();
        ResourceActionPair fab = new ResourceActionPair(fromYAMLFile, "b", "b");
        a.add(fab);
        a.add(new ResourceActionPair(fromYAMLFile, "a", "b"));

        HashSet<ResourceActionPair> b = new HashSet<ResourceActionPair>();
        ResourceActionPair feb = new ResourceActionPair(fromYAMLFile, "a", "b");
        b.add(feb);

        Set<ResourceActionPair> heavenFlowEntries = computeDifferenceSetHelper(a, b);

        assertNotNull(heavenFlowEntries);
        assertEquals(1, heavenFlowEntries.size());
        assertEquals(heavenFlowEntries.toArray()[0], fab);
    }

    @Test
    public void testComputeDifferenceAsymetric() throws Exception {
        API fromYAMLFile = apiFactory.createAPIBinding(new File("sample.yaml"), null, "https://localhost/api");

        HashSet<ResourceActionPair> a = new HashSet<ResourceActionPair>();
        ResourceActionPair fab = new ResourceActionPair(fromYAMLFile, "b", "b");
        a.add(fab);

        HashSet<ResourceActionPair> b = new HashSet<ResourceActionPair>();
        ResourceActionPair feb = new ResourceActionPair(fromYAMLFile, "a", "b");
        b.add(feb);

        Set<ResourceActionPair> heavenFlowEntries = computeDifferenceSetHelper(a, b);

        assertNotNull(heavenFlowEntries);
        assertEquals(1, heavenFlowEntries.size());
        assertEquals(heavenFlowEntries.toArray()[0], fab);
    }
}
