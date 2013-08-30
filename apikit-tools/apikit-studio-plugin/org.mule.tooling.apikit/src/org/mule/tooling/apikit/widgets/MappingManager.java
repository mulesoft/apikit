package org.mule.tooling.apikit.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;


public enum MappingManager {
    
    INSTANCE;
    
    public Collection<Resource> retrieveResources(Raml ramlSpec) {
        List<Resource> resources = new ArrayList<Resource>();
        for (Resource resource : ramlSpec.getResources().values()) {
            resources.add(resource);
            resources.addAll(retrieveResourcesFrom(resource));
        }
        return resources;
    }
    
    public Collection<? extends Resource> retrieveResourcesFrom(Resource resource) {
        List<Resource> resources = new ArrayList<Resource>();
        for (Resource innerResource : resource.getResources().values()) {
            resources.add(innerResource);
            resources.addAll(retrieveResourcesFrom(innerResource));
        }
        return resources;
    }

    public Collection<Action> getActions(Resource resource) {
        return resource.getActions().values();
    }
    
    public Collection<Action> getAllActions() {
        ActionType[] values = ActionType.values();
        ArrayList<Action> actions = new ArrayList<Action>();
        for (ActionType actionType : values) {
            Action action = new Action();
            action.setType(actionType);
            actions.add(action);
        }
        return actions;
    }
}
