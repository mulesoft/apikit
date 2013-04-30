/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
