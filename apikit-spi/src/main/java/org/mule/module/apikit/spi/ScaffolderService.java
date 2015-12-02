/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.spi;

import java.io.File;
import java.util.List;

/**
 * Extension (SPI) for the APIKit Maven Module
 */
public interface ScaffolderService
{

    /**
     * Modifies or creates the Mule config files which are contained in the appDir directory
     * by running the scaffolder on the ramlFiles passed as parameter.
     *
     * @param ramlFiles the ramlFiles to which the scaffolder will be run on
     * @param appDir    the directory which contained the generated Mule config files
     * @param domainDir the directory which contained the domain used by the mule config files
     * @param muleVersion used to know which type of endpoint (InboundEndpoint or Listener) that the scaffolder should create in case the xml is not provided. If this param is null, listeners will be used.
     */
    void executeScaffolder(List<File> ramlFiles, File appDir, File domainDir, String muleVersion);

}
