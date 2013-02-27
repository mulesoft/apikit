/**
 * Mule Rest Module
 *
 * Copyright 2011-2012 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package org.mule.module.apikit.rest;

public enum RestParameterType {
    /**
     * The input is part of the URI itself, specifically the URI portion which corresponds to the {name} in the
     * API path. All path parameters are mandatory and must be non-zero length.
     */
    PATH,

    /**
     * The input is specified by a key/value query param in the form of {key}={value}. Multiple query parameters are
     * separated by ???&??? delimiters.
     */
    QUERY,

    /**
     *
     */
    HEADER
}
