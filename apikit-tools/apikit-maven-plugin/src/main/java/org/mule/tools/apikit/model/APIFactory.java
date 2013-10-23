package org.mule.tools.apikit.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

public class APIFactory
{
    private Map<File, API> apis = new HashMap<File, API>();

    public API createAPIBinding(File yamlFile, File xmlFile, String path)
    {
        return createAPIBinding(yamlFile, xmlFile, path, null);
    }

    public API createAPIBinding(File yamlFile, File xmlFile, String path, APIKitConfig config)
    {
        Validate.notNull(yamlFile);
        if(apis.containsKey(yamlFile))
        {
            API api = apis.get(yamlFile);
            if(api.getXmlFile() == null && xmlFile != null)
            {
                api.setXmlFile(xmlFile);
            }

            api.setConfig(config);

            return api;
        }

        API api = new API(yamlFile, xmlFile, path, config);
        apis.put(yamlFile, api);
        return api;
    }

}
