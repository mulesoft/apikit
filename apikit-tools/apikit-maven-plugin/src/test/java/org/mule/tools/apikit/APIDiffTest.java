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
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.input.APIDiff;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;
import org.mule.tools.apikit.output.GenerationModel;

import java.io.File;
import java.util.HashMap;
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

    private Set<ResourceActionMimeTypeTriplet> computeDifferenceSetHelper(HashMap<ResourceActionMimeTypeTriplet, GenerationModel> a, HashSet<ResourceActionMimeTypeTriplet> b) {
        RAMLFilesParser RAMLFilesParser = mock(RAMLFilesParser.class);
        when(RAMLFilesParser.getEntries()).thenReturn(a);

        MuleConfigParser muleConfigParser = mock(MuleConfigParser.class);
        when(muleConfigParser.getEntries()).thenReturn(b);

        return new APIDiff(RAMLFilesParser.getEntries().keySet(), muleConfigParser.getEntries()).getEntries();
    }

    @Test
    public void testComputeDifferenceEmpty() throws Exception {
        HashMap<ResourceActionMimeTypeTriplet, GenerationModel> a = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
        HashSet<ResourceActionMimeTypeTriplet> b = new HashSet<ResourceActionMimeTypeTriplet>();

        Set<ResourceActionMimeTypeTriplet> heavenFlowEntries = computeDifferenceSetHelper(a, b);

        assertNotNull(heavenFlowEntries);
        assertTrue(heavenFlowEntries.isEmpty());
    }

    @Test
    public void testComputeDifference() throws Exception {
        API fromRAMLFile = apiFactory.createAPIBinding(new File("sample.raml"), null, "http://localhost:8080", "/api/*",  null);

        HashMap<ResourceActionMimeTypeTriplet, GenerationModel> a = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
        ResourceActionMimeTypeTriplet fab = new ResourceActionMimeTypeTriplet(fromRAMLFile, "a", "b");
        a.put(fab, mock(GenerationModel.class));

        HashSet<ResourceActionMimeTypeTriplet> b = new HashSet<ResourceActionMimeTypeTriplet>();
        ResourceActionMimeTypeTriplet feb = new ResourceActionMimeTypeTriplet(fromRAMLFile, "a", "b");
        b.add(feb);

        Set<ResourceActionMimeTypeTriplet> heavenFlowEntries = computeDifferenceSetHelper(a, b);

        assertEquals(feb, fab);
        assertNotNull(heavenFlowEntries);
        assertEquals(0, heavenFlowEntries.size());
    }

    @Test
    public void testComputeDifferenceMismatching() throws Exception {
        API fromRAMLFile = apiFactory.createAPIBinding(new File("sample.raml"), null, "http://localhost:8080", "/api/*",  null);

        HashMap<ResourceActionMimeTypeTriplet, GenerationModel> a = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
        ResourceActionMimeTypeTriplet fab = new ResourceActionMimeTypeTriplet(fromRAMLFile, "b", "b");
        a.put(fab, mock(GenerationModel.class));
        a.put(new ResourceActionMimeTypeTriplet(fromRAMLFile, "a", "b"), mock(GenerationModel.class));

        HashSet<ResourceActionMimeTypeTriplet> b = new HashSet<ResourceActionMimeTypeTriplet>();
        ResourceActionMimeTypeTriplet feb = new ResourceActionMimeTypeTriplet(fromRAMLFile, "a", "b");
        b.add(feb);

        Set<ResourceActionMimeTypeTriplet> heavenFlowEntries = computeDifferenceSetHelper(a, b);

        assertNotNull(heavenFlowEntries);
        assertEquals(1, heavenFlowEntries.size());
        assertEquals(heavenFlowEntries.toArray()[0], fab);
    }

    @Test
    public void testComputeDifferenceAsymetric() throws Exception {
        API fromRAMLFile = apiFactory.createAPIBinding(new File("sample.raml"), null, "http://localhost:8080", "/api/*",  null);

        HashMap<ResourceActionMimeTypeTriplet, GenerationModel> a = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
        ResourceActionMimeTypeTriplet fab = new ResourceActionMimeTypeTriplet(fromRAMLFile, "b", "b");
        a.put(fab, mock(GenerationModel.class));

        HashSet<ResourceActionMimeTypeTriplet> b = new HashSet<ResourceActionMimeTypeTriplet>();
        ResourceActionMimeTypeTriplet feb = new ResourceActionMimeTypeTriplet(fromRAMLFile, "a", "b");
        b.add(feb);

        Set<ResourceActionMimeTypeTriplet> heavenFlowEntries = computeDifferenceSetHelper(a, b);

        assertNotNull(heavenFlowEntries);
        assertEquals(1, heavenFlowEntries.size());
        assertEquals(heavenFlowEntries.toArray()[0], fab);
    }
}
