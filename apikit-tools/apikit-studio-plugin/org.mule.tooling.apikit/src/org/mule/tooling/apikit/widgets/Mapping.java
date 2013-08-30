/**
 * 
 */
package org.mule.tooling.apikit.widgets;

import org.mule.tooling.model.messageflow.Flow;
import org.raml.model.Action;
import org.raml.model.Resource;

/**
 * Maps resource and their actions to flow names.
 * 
 */
public class Mapping {

    private Resource resource;

    private Action action;

    private Flow flow;

    public Mapping() {

    }

    public Mapping(Resource resource, Action action, Flow flow) {
        this.setResource(resource);
        this.setAction(action);
        this.setFlow(flow);
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Flow getFlow() {
        return flow;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

}
