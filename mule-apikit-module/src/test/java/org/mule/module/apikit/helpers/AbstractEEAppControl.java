/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import org.mule.test.infrastructure.process.rules.MuleDeployment;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.ArrayUtils.add;
import static org.apache.commons.lang3.ArrayUtils.addAll;
import static org.mule.distributions.tests.DistributionFinder.findDistribution;

public class AbstractEEAppControl {

  public static final String LOCAL_REPOSITORY = "localRepository";

  public static MuleDeployment.Builder builderWithDefaultConfig() {
    MuleDeployment.Builder builder = MuleDeployment.builder(findDistribution());
    builder.withProperties(getDefaultArguments());
    return builder;
  }

  public static Optional<String> getLocalRepository() {
    final String localRepository = System.getProperty(LOCAL_REPOSITORY);
    if (localRepository != null) {
      return of(localRepository);
    }
    return empty();
  }

  protected static String[] getArgumentsIncludingDefaults(Boolean addRepositoryLocation, String... arguments) {
    Optional<String> localRepository = getLocalRepository();

    for (Map.Entry<Object, Object> sysPropEntry : System.getProperties().entrySet()) {
      final String key = (String) sysPropEntry.getKey();
      if (key.startsWith("-M")) {
        arguments = add(arguments, key + "=" + sysPropEntry.getValue());
      }
    }

    if (localRepository.isPresent() && addRepositoryLocation) {
      arguments = addAll(arguments,
                         new String[] {
                             "-M-DmuleRuntimeConfig.maven.repositoryLocation=" + localRepository.get()
                         });
    }
    return arguments;
    /*   return addAll(arguments,
            new String[] {
                    "-M-DmuleRuntimeConfig.maven.repositories.mavenCentral.url=https://repo.maven.apache.org/maven2/",
                    "-M-DmuleRuntimeConfig.maven.repositories.muleSoftPublic.url=https://repository.mulesoft.org/nexus/content/repositories/public/",
                    "-M-DmuleRuntimeConfig.maven.repositories.springReactorSnapshot.url=http://repo.spring.io/snapshot/",
                    "-M-DmuleRuntimeConfig.maven.repositories.muleSoftPrivateCi.url=https://repository.mulesoft.org/nexus/content/repositories/ci-snapshots/",
                    "-M-DmuleRuntimeConfig.maven.repositories.muleSoftPrivateCi.username=" + getPrivateRepoUsername(),
                    "-M-DmuleRuntimeConfig.maven.repositories.muleSoftPrivateCi.password=" + getPrivateRepoPassword(),
                    "-M-DmuleRuntimeConfig.maven.repositories.muleSoftPrivateMasterCi.url=https://repository-master.mulesoft.org/nexus/content/repositories/ci-snapshots/",
                    "-M-DmuleRuntimeConfig.maven.repositories.muleSoftPrivateMasterCi.username=" + getPrivateRepoUsername(),
                    "-M-DmuleRuntimeConfig.maven.repositories.muleSoftPrivateMasterCi.password=" + getPrivateRepoPassword(),
                    "-M-DmuleRuntimeConfig.maven.repositories.muleSoftPrivate.url=https://repository.mulesoft.org/nexus/content/repositories/private/",
                    "-M-DmuleRuntimeConfig.maven.repositories.muleSoftPrivate.username=" + getPrivateRepoUsername(),
                    "-M-DmuleRuntimeConfig.maven.repositories.muleSoftPrivate.password=" + getPrivateRepoPassword(),
                    "-M-Dmule.verbose.exceptions=true"});*/
  }

  public static String[] getArgumentsIncludingDefaults(String... arguments) {
    return getArgumentsIncludingDefaults(true, arguments);
  }

  protected static Map<String, String> getDefaultArguments() {
    return Arrays.stream(getArgumentsIncludingDefaults()).map(property -> property.split("="))
        .collect(Collectors.toMap(e -> e[0], e -> e[1]));
  }
}
