/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.ResourceActionPair;
import org.raml.model.Action;
import org.raml.model.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenerationModelProvider {
    private Map<ResourceActionPair, GenerationModel> map = new HashMap<ResourceActionPair, GenerationModel>();

    public List<GenerationModel> generate(Set<ResourceActionPair> generate) {
        Set<GenerationModel> generationModels = new HashSet<GenerationModel>();
        for (ResourceActionPair resourceActionPair : generate) {
            GenerationModel generationModel = map.get(resourceActionPair);
            if (generationModel != null) {
                generationModels.add(generationModel);
            }
        }
        List<GenerationModel> models = new ArrayList<GenerationModel>(generationModels);
        Collections.sort(models);
        return models;
    }

    public void add(API api, ResourceActionPair entry, Resource resource, Action action) {
        GenerationModel generationModel = new GenerationModel(api, resource, action);
        this.map.put(entry, generationModel);
    }

}
