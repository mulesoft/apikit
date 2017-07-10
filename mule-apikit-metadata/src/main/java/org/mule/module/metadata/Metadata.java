/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.metadata;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.builder.FunctionTypeBuilder;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.message.MessageMetadataType;
import org.mule.metadata.message.MessageMetadataTypeBuilder;
import org.mule.metadata.message.MuleEventMetadataType;
import org.mule.metadata.message.MuleEventMetadataTypeBuilder;

public class Metadata
{
    private Metadata() { }

    // TODO: 7/10/17 Add parameters
    public static FunctionType of() {

        MetadataType payloadMetadata = createPayloadMetadata();
        MetadataType headersMetadata = createHeadersMetadata();

        // Input Message Metadata
        MessageMetadataType messageMetadata = new MessageMetadataTypeBuilder()
                .payload(payloadMetadata)
                .attributes(headersMetadata)
                .build();

        // Input Event Metadata
        MuleEventMetadataType inputMetadata = new MuleEventMetadataTypeBuilder()
                .message(messageMetadata)
                .build();

        MuleEventMetadataType outputMetadata = new MuleEventMetadataTypeBuilder()
                .build();

        // FunctionType
        FunctionTypeBuilder functionTypeBuilder = BaseTypeBuilder.create(MetadataFormat.JSON).functionType();
        FunctionType function = functionTypeBuilder
                .addParameterOf("inputMetadata", inputMetadata)
                .returnType(outputMetadata)
                .build();

        return function;
    }

    private static MetadataType createHeadersMetadata()
    {

//        headers:
//          X-Amount:
//            type: integer
//            required: true

        ObjectTypeBuilder headersBuilder = BaseTypeBuilder.create(MetadataFormat.JSON).objectType();

        headersBuilder.addField()
                .key("X-Amount")
                .value().objectType().addField()
                    .required(true)
                    .value().numberType().integer();

        return headersBuilder.build();
    }


    private static MetadataType createPayloadMetadata() {

//        body:
//          application/json:
//            type: Book
//            example: {
//              "title" : "El Salvaje",
//              "author" : "Guillermo Arriaga"
//            }

        ObjectTypeBuilder objectBuilder = BaseTypeBuilder.create(MetadataFormat.JSON).objectType();

        objectBuilder.addField()
                .required(true)
                .key("title")
                .value().stringType();

        objectBuilder.addField()
                .required(true)
                .key("author")
                .value().stringType();

        objectBuilder.addField()
                .required(false)
                .key("available")
                .value().booleanType();

        return objectBuilder.build();
    }

}
