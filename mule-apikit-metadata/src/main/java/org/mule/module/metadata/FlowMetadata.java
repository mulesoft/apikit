package org.mule.module.metadata;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.builder.FunctionTypeBuilder;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.message.MessageMetadataType;
import org.mule.metadata.message.MessageMetadataTypeBuilder;
import org.mule.metadata.message.MuleEventMetadataType;
import org.mule.metadata.message.MuleEventMetadataTypeBuilder;
import org.mule.module.metadata.interfaces.MetadataSource;
import org.mule.module.metadata.model.Payload;
import org.mule.module.metadata.model.RamlCoordinate;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.Map;
import java.util.Optional;

public class FlowMetadata implements MetadataSource
{

    private static final String PARAMETER_INPUT_METADATA = "inputMetadata";
    private static final String ATTRIBUTES_QUERY_PARAMETERS = "queryParameters";
    private static final String ATTRIBUTES_HEADERS = "headers";
    private static final String ATTRIBUTES_URI_PARAMETERS = "uriParameters";


    private IAction action;
    private RamlCoordinate coordinate;

    public FlowMetadata(IAction action, RamlCoordinate coordinate) {
        this.action = action;
        this.coordinate = coordinate;
    }

    @Override
    public Optional<FunctionType> getMetadata()
    {
        MuleEventMetadataType muleInputEventMetadata = inputMetadata(action, coordinate);
        MuleEventMetadataType muleOutputEventMetadata = outputMetadata(action, coordinate);

        // FunctionType
        FunctionTypeBuilder functionTypeBuilder = BaseTypeBuilder.create(MetadataFormat.JAVA).functionType();
        FunctionType function = functionTypeBuilder
                .addParameterOf(PARAMETER_INPUT_METADATA, muleInputEventMetadata)
                .returnType(muleOutputEventMetadata)
                .build();

        return Optional.of(function);
    }

    private MuleEventMetadataType inputMetadata(IAction action, RamlCoordinate coordinate) {

        MessageMetadataType messageMetadataType = new MessageMetadataTypeBuilder()
                .payload(getInputPayload(action, coordinate))
                .attributes(getInputAttributes(action))
                .build();

        return new MuleEventMetadataTypeBuilder()
                .message(messageMetadataType)
                .build();
    }

    private MuleEventMetadataType outputMetadata(IAction action, RamlCoordinate coordinate)
    {
        MessageMetadataType message = new MessageMetadataTypeBuilder()
                .payload(getOutputPayload(action))
                .build();

        return new MuleEventMetadataTypeBuilder()
                .message(message)
//                .addVariable("outboundHeadersMapName", getHeadersOutputMetadata())
                .build();

    }

    private ObjectTypeBuilder getQueryParameters(IAction action) {

        ObjectTypeBuilder queryParametersMetadataBuilder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();
        for (Map.Entry<String, IParameter> entry : action.getQueryParameters().entrySet()) {
            queryParametersMetadataBuilder.addField()
                    .key(entry.getKey())
                    .value().anyType();
        }

        return queryParametersMetadataBuilder;
    }

    private ObjectTypeBuilder getHeaders(IAction action) {

        ObjectTypeBuilder headersMetadataBuilder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();
        for (Map.Entry<String, IParameter> entry : action.getHeaders().entrySet()) {
            headersMetadataBuilder.addField()
                    .key(entry.getKey())
                    .value().anyType();
        }

        return headersMetadataBuilder;
    }

    private ObjectType getInputAttributes(IAction action) {

        ObjectTypeBuilder attributesBuilder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();
        attributesBuilder.addField()
                .key(ATTRIBUTES_QUERY_PARAMETERS)
                .value(getQueryParameters(action));
        attributesBuilder.addField()
                .key(ATTRIBUTES_HEADERS)
                .value(getHeaders(action));
        attributesBuilder.addField()
                .key(ATTRIBUTES_URI_PARAMETERS)
                .value().anyType(); // TODO: Metadata for UriParameters

        return attributesBuilder.build();
    }


    private MetadataType getOutputPayload(IAction action) {

        IMimeType mimeType = action.getResponses().values().stream()
                .filter(response -> response.getBody() != null)
                .flatMap(response -> response.getBody().values().stream())
                .findFirst()
                .orElse(null);

        return Payload.metadata(mimeType);
    }

    private MetadataType getInputPayload(IAction action, RamlCoordinate coordinate) {

        IMimeType mimeType = null;

        if (action.hasBody()) {

            if (action.getBody().size() == 1) {

                mimeType = action.getBody().values().stream()
                        .findFirst()
                        .orElse(null);

            } else if (coordinate.getMediaType() != null) {

                mimeType = action.getBody().get(coordinate.getMediaType());
            }

            return Payload.metadata(mimeType);
        }

        return null;
    }
}
