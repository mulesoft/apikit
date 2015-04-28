/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationModel;

public class RAMLFilesParserTest
{

    @Test
    public void apiWithWarningsShouldBeValid()
    {
        final InputStream resourceAsStream =
                RAMLFilesParserTest.class.getClassLoader().getResourceAsStream(
                        "scaffolder/apiWithWarnings.raml");
        
        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));

        HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
        streams.put(new File("hello"), resourceAsStream);

        RAMLFilesParser ramlFilesParser = new RAMLFilesParser(log, streams, new APIFactory());

        Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = ramlFilesParser.getEntries();
        assertNotNull(entries);
        assertEquals(1, entries.size());
    }
    
}
