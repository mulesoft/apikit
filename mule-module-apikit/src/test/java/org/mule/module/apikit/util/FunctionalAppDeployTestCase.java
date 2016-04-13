/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mule.api.config.MuleProperties;
import org.mule.module.launcher.DeploymentListener;
import org.mule.module.launcher.MuleDeploymentService;
import org.mule.module.launcher.MulePluginClassLoaderManager;
import org.mule.module.launcher.application.Application;
import org.mule.module.launcher.application.ApplicationStatus;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.probe.JUnitProbe;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Prober;
import org.mule.util.FileUtils;

import java.io.File;
import java.io.IOException;

public class FunctionalAppDeployTestCase extends AbstractMuleContextTestCase
{

    protected static final int DEPLOYMENT_TIMEOUT = 10000;

    protected File muleHome;
    protected File appsDir;
    protected File domainsDir;
    protected MuleDeploymentService deploymentService;
    protected DeploymentListener applicationDeploymentListener;

    @Override
    public void doSetUp() throws Exception
    {
        super.doSetUp();
        // set up some mule home structure
        final String tmpDir = System.getProperty("java.io.tmpdir");
        muleHome = new File(new File(tmpDir, "mule-home"), getClass().getSimpleName() + System.currentTimeMillis());
        appsDir = new File(muleHome, "apps");
        appsDir.mkdirs();
        domainsDir = new File(muleHome, "domains");
        domainsDir.mkdirs();
        System.setProperty(MuleProperties.MULE_HOME_DIRECTORY_PROPERTY, muleHome.getCanonicalPath());

        new File(muleHome, "lib/shared/default").mkdirs();

        applicationDeploymentListener = mock(DeploymentListener.class);
        //domainDeploymentListener = mock(DeploymentListener.class);
        deploymentService = new MuleDeploymentService(new MulePluginClassLoaderManager());
        deploymentService.addDeploymentListener(applicationDeploymentListener);
        //deploymentService.addDomainDeploymentListener(domainDeploymentListener);
    }

    @Override
    public void doTearDown() throws Exception
    {
        // comment out the deletion to analyze results after test is done
        if (deploymentService != null)
        {
            deploymentService.stop();
        }
        FileUtils.deleteTree(muleHome);
        //super.doTearDown();

        // this is a complex classloader setup and we can't reproduce standalone Mule 100%,
        // so trick the next test method into thinking it's the first run, otherwise
        // app resets CCL ref to null and breaks the next test
        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
    }

    protected void deployExplodedApp(File appDir, String appName) throws IOException
    {
            // Uses a temp folder to deploy the app to avoid the DeploymentService to attempt
            // to deploy an incomplete app
            File tempAppFolder = new File(muleHome, appName);
            FileUtils.copyDirectory(appDir, tempAppFolder);
            File appFolder = new File(appsDir, appName);
            tempAppFolder.renameTo(appFolder);
        }

    protected void assertApplicationDeploymentSuccess(DeploymentListener listener, String artifactName)
    {
        assertDeploymentSuccess(listener, artifactName);
        assertStatus(artifactName, ApplicationStatus.STARTED);
    }

    private void assertDeploymentSuccess(final DeploymentListener listener, final String artifactName)
    {
        Prober prober = new PollingProber(DEPLOYMENT_TIMEOUT, 100);
        prober.check(new JUnitProbe()
        {
            @Override
            protected boolean test() throws Exception
            {
                verify(listener, times(1)).onDeploymentSuccess(artifactName);
                return true;
            }

            @Override
            public String describeFailure()
            {
                return "Failed to deploy application: " + artifactName + System.lineSeparator() + super.describeFailure();
            }
        });
    }

    private void assertStatus(String appName, final ApplicationStatus status)
    {
        final Application application = findApp(appName);
        Prober prober = new PollingProber(DEPLOYMENT_TIMEOUT, 100);
        prober.check(new JUnitProbe()
        {
            @Override
            protected boolean test() throws Exception
            {
                assertThat(application.getStatus(), is(status));
                return true;
            }

            @Override
            public String describeFailure()
            {
                return String.format("Application %s was expected to be in status %s but was %s instead",
                                     application.getArtifactName(), status.name(), application.getStatus().name());
            }
        });

    }

    private Application findApp(final String appName)
    {
        final Application app = deploymentService.findApplication(appName);
        assertNotNull(app);
        return app;
    }

}
