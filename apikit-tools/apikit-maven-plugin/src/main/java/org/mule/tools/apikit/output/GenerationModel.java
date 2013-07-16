package org.mule.tools.apikit.output;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.mule.tools.apikit.model.API;
import org.raml.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GenerationModel {

    private static final char FLOW_NAME_SEPARATOR = ':';

    private final String verb;
    private Action action;
    private Resource resource;
    private List<String> splitPath;
    private API api;

    public GenerationModel(API api, Resource resource, Action action) {
        this.api = api;
        Validate.notNull(api);
        Validate.notNull(resource);
        Validate.notNull(action);
        Validate.notNull(action.getType());
        Validate.notNull(resource.getUri());
        Validate.notNull(action.getType());

        this.resource = resource;
        this.action = action;
        this.splitPath = new ArrayList<String>(Arrays.asList(this.resource.getUri().split("/")));
        this.verb = action.getType().toString();
        splitPath.remove(0);
        splitPath.remove(0);
    }

    public String getVerb() {
        return verb;
    }

    public String getStringFromActionType() {
        switch (action.getType()) {
            case GET:
                return "retrieve";
            case POST:
                return "update";
            case PUT:
                return "create";
            case DELETE:
                return "delete";
            default:
                return action.getType().toString().toLowerCase();
        }
    }

    public String getExample() {
        String exampleWrappee = this.getExampleWrappee();

        if (exampleWrappee != null) {
            return exampleWrappee;
        } else {
            return "Hello world!";
        }

    }

    private String getExampleWrappee() {
        Map<String, Response> responses = action.getResponses();

        Response response = responses.get("200");

        if (response == null || response.getBody() == null) {
            for (Response response1 : responses.values()) {
                if (response1.getBody() != null) {
                    Map<String, MimeType> responseBody1 = response1.getBody();
                    MimeType mimeType = responseBody1.get("application/json");
                    if (mimeType != null && mimeType.getExample() != null) {
                        return mimeType.getExample();
                    } else {
                        for (MimeType type : responseBody1.values()) {
                            if (type.getExample() != null) {
                                return type.getExample();
                            }
                        }

                    }
                }
            }
        }

        if (response != null && response.getBody() != null) {
            Map<String, MimeType> body = response.getBody();
            MimeType mimeType = body.get("application/json");
            if (mimeType != null && mimeType.getExample() != null) {
                return mimeType.getExample();
            }

            for (MimeType mimeType2 : response.getBody().values()) {
                if (mimeType2 != null && mimeType2.getExample() != null) {
                    return mimeType2.getExample();
                }
            }
        }

        return null;

    }

    public String getName() {
        StringBuilder name = new StringBuilder();
        name.append(this.getStringFromActionType());
        String name1 = this.resource.getName();

        if (name1 == null) {
            StringBuffer buff = new StringBuffer();

            for (String i : this.splitPath) {
                buff.append(StringUtils.capitalize(i));
            }

            name1 = buff.toString();

        }

        name.append(name1);

        return name.toString().replace(" ", "");
    }

    public String getRelativeURI() {
        return "/" + StringUtils.join(splitPath.toArray(), "/");
    }

    public API getApi() {
        return api;
    }

    public String getFlowName() {
        StringBuilder flowName = new StringBuilder("");
        flowName.append(action.getType().toString().toLowerCase())
                .append(FLOW_NAME_SEPARATOR)
                .append(resource.getRelativeUri());

        if(api.getConfig() != null && !StringUtils.isEmpty(api.getConfig().getName())) {
            flowName.append(FLOW_NAME_SEPARATOR)
                    .append(api.getConfig().getName());
        }
        return flowName.toString();
    }
}
