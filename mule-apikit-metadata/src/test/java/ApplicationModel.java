/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
import org.mule.runtime.config.spring.dsl.model.ComponentModel;

import java.util.Optional;

/**
 *
 */
public interface ApplicationModel
{

    /**
     *
     * @return
     */
    ComponentModel findRootComponentModel();

    /**
     *
     * @param name
     * @return
     */
    Optional<ComponentModel> findNamedComponent(String name);

    Optional<String> findTypesData();
}