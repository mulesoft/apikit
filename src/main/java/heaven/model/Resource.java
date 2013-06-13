package heaven.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heaven.model.parameter.UriParameter;
import heaven.parser.ParseException;
import org.apache.commons.lang.ArrayUtils;

public class Resource
{

    private String parentUri;
    private String name;
    private String relativeUri;
    private Map<String, UriParameter> uriParameters = new HashMap<String, UriParameter>();
    private ResourceMap resources = new ResourceMap();
    private Map<String, Action> actions = new HashMap<String, Action>();
    private List<?> uses = new ArrayList();

    private static final String[] ACTIONS = {"get", "post", "put", "delete", "head"};
    private static final List<String> VALID_KEYS;

    static
    {
        String[] keys = (String[]) ArrayUtils.addAll(ACTIONS, new String[] {"name", "uriParameters", "use"});
        VALID_KEYS = Arrays.asList(keys);
    }

    public Resource(String relativeUri, Map<String, ?> descriptor, String parentUri)
    {
        this.parentUri = parentUri;
        this.relativeUri = relativeUri;
        name = (String) descriptor.get("name");
        if (descriptor.containsKey("uses"))
        {
            uses = (List<?>) descriptor.get("use");
        }
        if (descriptor.containsKey("uriParameters"))
        {
            populateUriParameters((Map<String, ?>) descriptor.get("uriParameters"));
        }

        populateAction(descriptor);

        resources.populate(descriptor, this.getUri());

        List<String> invalidKeys = new ArrayList<String>();
        for (String key : descriptor.keySet())
        {
            if (!key.startsWith("/") && !VALID_KEYS.contains(key))
            {
                invalidKeys.add(key);
            }
        }
        if (!invalidKeys.isEmpty())
        {
            throw new ParseException("invalid top level keys: " + invalidKeys);
        }
    }

    private void populateUriParameters(Map<String, ?> descriptor)
    {
        for (String param : descriptor.keySet())
        {
            //TODO do proper parsing with 3rd party lib
            if (relativeUri.indexOf("{" + param + "}") == -1)
            {
                throw new ParseException(String.format("Relative URI (%s) does not define \"%s\" parameter",
                                                       relativeUri, param));
            }
            uriParameters.put(param, new UriParameter((Map<String, ?>) descriptor.get(param)));
        }
    }

    private void populateAction(Map descriptor)
    {
        boolean actionDefined = false;
        for (String name : ACTIONS)
        {
            if (descriptor.containsKey(name))
            {
                actions.put(name, new Action((Map) descriptor.get(name)));
                actionDefined = true;
            }
        }
        if (!actionDefined)
        {
            throw new ParseException("at least one action must be defined");
        }
    }

    public String getName()
    {
        return name;
    }

    public String getRelativeUri()
    {
        return relativeUri;
    }

    public String getUri()
    {
        if (parentUri.endsWith("/"))
        {
            return parentUri + relativeUri.substring(1);
        }
        return parentUri + relativeUri;
    }

    public List<?> getUses()
    {
        return uses;
    }

    public Action getAction(String name)
    {
        return actions.get(name);
    }

    public ResourceMap getResources()
    {
        return resources;
    }

    public Map<String, UriParameter> getUriParameters()
    {
        return uriParameters;
    }
}
