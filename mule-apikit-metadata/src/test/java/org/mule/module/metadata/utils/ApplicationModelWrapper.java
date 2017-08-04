package org.mule.module.metadata.utils;

import java.util.Optional;
import org.mule.runtime.config.spring.api.dsl.model.ComponentModel;

public interface ApplicationModelWrapper
{
    ComponentModel findRootComponentModel();

    Optional<ComponentModel> findNamedComponent(String var1);

    Optional<String> findTypesData();
}
