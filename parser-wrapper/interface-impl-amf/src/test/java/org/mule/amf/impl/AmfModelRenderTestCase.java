package org.mule.amf.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mule.raml.interfaces.model.api.ApiRef;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static java.nio.file.Files.readAllBytes;

public class AmfModelRenderTestCase {
    @Test
    public void renderTestCase() throws Exception {
        String folderLocation = AmfModelRenderTestCase.class.getResource("").toURI() + "amf-model-render/";
        String apiLocation = folderLocation + "api-to-render.raml";
        String goldenAmfModel = new String(readAllBytes(Paths.get(new URI(folderLocation + "golden-amf-model.json"))), StandardCharsets.UTF_8);

        ApiRef apiRef = ApiRef.create(apiLocation);
        String amfModel = ParserWrapperAmf.create(apiRef, true).getAmfModel();

        Assert.assertEquals(amfModel, goldenAmfModel);
    }
}
