/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10.model;

import com.google.common.base.Function;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.StringTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.internal.impl.v10.type.TypeId;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.newHashSet;
import static org.raml.v2.internal.impl.v10.type.TypeId.ARRAY;
import static org.raml.v2.internal.impl.v10.type.TypeId.OBJECT;

public class ParameterImpl implements IParameter
{

    private TypeDeclaration typeDeclaration;
    private Collection<String> scalarTypes;

    public ParameterImpl(TypeDeclaration typeDeclaration)
    {
        this.typeDeclaration = typeDeclaration;

        Set<TypeId> typeIds = newHashSet(TypeId.values());
        typeIds.remove(OBJECT);
        typeIds.remove(ARRAY);

        scalarTypes = transform(typeIds, new Function<TypeId, String>() {
            @Nullable
            @Override
            public String apply(@Nullable TypeId input) {
                return input.getType();
            }
        });
    }

    @Override
    public boolean validate(String value)
    {
        List<ValidationResult> results = typeDeclaration.validate(value);
        return results.isEmpty();
    }

    @Override
    public String message(String value)
    {
        List<ValidationResult> results = typeDeclaration.validate(value);
        return results.isEmpty() ? "OK" : results.get(0).getMessage();
    }

    @Override
    public boolean isRequired()
    {
        return typeDeclaration.required();
    }

    @Override
    public String getDefaultValue()
    {
        return typeDeclaration.defaultValue();
    }

    @Override
    public boolean isRepeat()
    {
        // only available in RAML 0.8
        return false;
    }

    @Override
    public boolean isArray()
    {
        return typeDeclaration instanceof ArrayTypeDeclaration;
    }

    @Override
    public String getDisplayName()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDescription()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getExample()
    {
        if (typeDeclaration.example() == null)
        {
            return null;
        }
        return typeDeclaration.example().value();
    }

    @Override
    public Map<String, String> getExamples()
    {
        Map<String, String> examples = new LinkedHashMap<>();
        for (ExampleSpec example : typeDeclaration.examples())
        {
            examples.put(example.name(), example.value());
        }
        return examples;
    }

    @Override
    public Object getInstance()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStringArray() {
        return isArray() && ((ArrayTypeDeclaration) typeDeclaration).items() instanceof StringTypeDeclaration;
    }

    @Override
    public boolean isScalar() {
        return scalarTypes.contains(typeDeclaration.type());
    }

    @Override
    public boolean isFacetArray(String facet) {
        if (typeDeclaration instanceof ObjectTypeDeclaration) {
            for (TypeDeclaration type : ((ObjectTypeDeclaration) typeDeclaration).properties()) {
                if (type.name().equals(facet)) return type instanceof ArrayTypeDeclaration;
            }
        }
        return false;
    }
}
