package org.mule.tools.apikit.output;

import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.ResourceActionPair;
import org.raml.model.Action;
import org.raml.model.Resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenerationModelProvider {
    private Map<ResourceActionPair, GenerationModel> map = new HashMap<ResourceActionPair, GenerationModel>();

    public Set<GenerationModel> generate(Set<ResourceActionPair> generate) {
        Set<GenerationModel> generationModels = new HashSet<GenerationModel>();
        for (ResourceActionPair resourceActionPair : generate) {
            GenerationModel generationModel = map.get(resourceActionPair);
            if (generationModel != null) {
                generationModels.add(generationModel);
            }
        }
        return  generationModels;
    }

    public void add(API api, ResourceActionPair entry, Resource resource, Action action) {
        GenerationModel generationModel = new GenerationModel(api, resource, action);
        this.map.put(entry, generationModel);
    }

}
