/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

import java.util.HashSet;
import java.util.Set;

public class APIDiff {
    private final Set<ResourceActionMimeTypeTriplet> ramlEntries;
    private final Set<ResourceActionMimeTypeTriplet> muleFlowEntries;
    private final Set<ResourceActionMimeTypeTriplet> difference;

    public APIDiff(Set<ResourceActionMimeTypeTriplet> ramlEntries, Set<ResourceActionMimeTypeTriplet> muleFlowEntries) {
        this.ramlEntries = ramlEntries;
        this.muleFlowEntries = muleFlowEntries;
        this.difference = computeDifference(this.ramlEntries, this.muleFlowEntries);
    }

    public Set<ResourceActionMimeTypeTriplet> getEntries() {
        return difference;
    }

    private Set<ResourceActionMimeTypeTriplet> computeDifference(Set<ResourceActionMimeTypeTriplet> ramlEntries,
                                                   Set<ResourceActionMimeTypeTriplet> flowEntries) {
        Set<ResourceActionMimeTypeTriplet> differenceToAdd = new HashSet<ResourceActionMimeTypeTriplet>(ramlEntries);
        differenceToAdd.removeAll(flowEntries);
        return differenceToAdd;
    }

}
