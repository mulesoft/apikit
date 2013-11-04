/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.mule.tools.apikit.model.ResourceActionPair;

import java.util.HashSet;
import java.util.Set;

public class APIDiff {
    private final Set<ResourceActionPair> yamlEntries;
    private final Set<ResourceActionPair> muleFlowEntries;
    private final Set<ResourceActionPair> difference;

    public APIDiff(Set<ResourceActionPair> yamlEntries, Set<ResourceActionPair> muleFlowEntries) {
        this.yamlEntries = yamlEntries;
        this.muleFlowEntries = muleFlowEntries;
        this.difference = computeDifference(yamlEntries, muleFlowEntries);
    }

    public APIDiff(RAMLFilesParser RAMLFilesParser, MuleConfigParser muleConfigParser) {
        this(RAMLFilesParser.getEntries(), muleConfigParser.getEntries());
    }

    public Set<ResourceActionPair> getEntries() {
        return difference;
    }

    private Set<ResourceActionPair> computeDifference(Set<ResourceActionPair> yamlEntries,
                                                   Set<ResourceActionPair> flowEntries) {
        Set<ResourceActionPair> differenceToAdd = new HashSet<ResourceActionPair>(yamlEntries);
        differenceToAdd.removeAll(flowEntries);
        return differenceToAdd;
    }

}
