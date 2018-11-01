/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import java.util.Optional;
import org.mule.module.apikit.metadata.api.MetadataSource;

public interface MetadataResolver {

  Optional<MetadataSource> getMetadataSource(ApiCoordinate coordinate, String httpStatusVar, String outboundHeadersVar);
}
