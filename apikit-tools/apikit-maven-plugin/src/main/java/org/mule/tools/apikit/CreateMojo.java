/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.commons.lang.Validate;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.Scanner;
import org.omg.CORBA.MARSHAL;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.*;
import java.util.*;

/**
 * Goal for apikit:create
 */
@Mojo(name = "create")
public class CreateMojo
        extends AbstractMojo {
    @Component
    private BuildContext buildContext;

    /**
     * Spec source directory to use as root of specInclude and specExclude patterns.
     */
    @Parameter(defaultValue = "${basedir}")
    private File specDirectory;

    /**
     * Spec source directory to use as root of muleInclude and muleExclude patterns.
     */
    @Parameter(defaultValue = "${basedir}")
    private File muleXmlDirectory;

    /**
     * Where to output the generated mule config files.
     */
    @Parameter(defaultValue = "${basedir}/src/main/app")
    private File muleXmlOutputDirectory;

    /**
     * Spec source directory to use as root of muleDomain.
     */
    @Parameter (property = "domainDirectory")
    private File domainDirectory;

    /**
     * Mule version that is being used.
     */
    private final static String MULE_VERSION = "3.7.0";
    @Parameter (property = "muleVersion")
    private String muleVersion = MULE_VERSION;

    private Log log;

    public void execute() throws MojoExecutionException
    {
        Validate.notNull(muleXmlDirectory, "Error: muleXmlDirectory parameter cannot be null");
        Validate.notNull(specDirectory, "Error: specDirectory parameter cannot be null");

        log = getLog();

        Main main = new Main();
        try
        {
            main.process(log,specDirectory,domainDirectory,muleXmlDirectory,muleXmlOutputDirectory);
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(e.getMessage());
        }
    }

}