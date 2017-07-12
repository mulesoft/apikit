package org.mule.module.metadata;

import org.mule.runtime.api.component.ComponentIdentifier;

public class ApikitIdentifiers
{
    private static final ComponentIdentifier FLOW =
            ComponentIdentifier.buildFromStringRepresentation("flow");

    private static final ComponentIdentifier APIKIT_CONFIG =
            ComponentIdentifier.buildFromStringRepresentation("apikit:config");

    private static final ComponentIdentifier APIKIT_FLOW_MAPPINGS =
            ComponentIdentifier.buildFromStringRepresentation("apikit:flow-mappings");

    private static final ComponentIdentifier APIKIT_FLOW_MAPPING =
            ComponentIdentifier.buildFromStringRepresentation("apikit:flow-mapping");


    public static boolean isFlow(ComponentIdentifier identifier) {
        return identifier.equals(FLOW)   ;
    }

    public static boolean isApikitConfig(ComponentIdentifier identifier) {
        return identifier.equals(APIKIT_CONFIG);
    }

    public static boolean isFlowMappings(ComponentIdentifier identifier) {
        return identifier.equals(APIKIT_FLOW_MAPPINGS);
    }

    public static boolean isFlowMapping(ComponentIdentifier identifier) {
        return identifier.equals(APIKIT_FLOW_MAPPING);
    }
}
