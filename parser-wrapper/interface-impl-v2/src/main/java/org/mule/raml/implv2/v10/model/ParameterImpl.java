/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10.model;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.datamodel.AnyTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.StringTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.UnionTypeDeclaration;
import org.raml.v2.internal.impl.v10.type.TypeId;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.valueOf;
import static java.lang.System.getProperty;
import static java.util.Collections.singletonList;
import static org.raml.v2.internal.impl.v10.type.TypeId.ARRAY;
import static org.raml.v2.internal.impl.v10.type.TypeId.OBJECT;

public class ParameterImpl implements IParameter
{

    private TypeDeclaration typeDeclaration;
    private Collection<String> scalarTypes;
    private Boolean required;
    private Optional<String> defaultValue;
    private final boolean validateNullInStringType;

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
        validateNullInStringType = valueOf(getProperty("apikit.validate.null.string.param", "false"));
    }

    @Override
    public boolean validate(String value)
    {
        List<ValidationResult> results = typeDeclaration.validate(value);
        return results.isEmpty();
    }

    private List<ValidationResult> validate(TypeDeclaration type, String value)
    {
        return type.validate(value);
    }

    @Override
    public void validate(String expectedKey, Object values, String parameterType) throws Exception {
        Collection<?> properties;

        if (values instanceof Iterable) {

            properties = newArrayList((Iterable) values);
        } else properties = singletonList(values);

        validateParam(typeDeclaration, expectedKey, properties, parameterType);
    }

    private void validateParam(TypeDeclaration type, String paramKey, Collection<?> paramValues, String parameterType) throws Exception
    {
        if (type instanceof ArrayTypeDeclaration) {
            validateArray((ArrayTypeDeclaration) type, paramKey, paramValues, parameterType);
        } else if (type instanceof UnionTypeDeclaration) {
            validateUnion((UnionTypeDeclaration) type, paramKey, paramValues, parameterType);
        } else if (!(type instanceof AnyTypeDeclaration)) {
            if (paramValues.size() > 1) {

                throw new Exception("Parameter " + paramKey + " is not an array");
            }
            Object value = paramValues.iterator().next();
            String paramValue = (value == null && validateNullInStringType && type instanceof StringTypeDeclaration) ? null : String.valueOf(value);
            List<ValidationResult> validationResults = validate(type, paramValue);
            if (!validationResults.isEmpty()) {
                String msg = String.format("Invalid value '%s' for %s %s. %s", paramValues, parameterType, paramKey, validationResults.get(0).getMessage());
                throw new Exception(msg);
            }
        }
    }

    private void validateArray(ArrayTypeDeclaration type, String paramKeym, Collection<?> paramValues, String parameterType) throws Exception {
        Integer minItems = type.minItems();
        int size = paramValues.size();
        if (minItems != null && minItems > size) throw new Exception("Expected min items " + minItems + " for " + paramKeym + " and got " + size);
        Integer maxItems = type.maxItems();
        if (maxItems != null && size > maxItems) throw new Exception("Expected max items " + maxItems + " for " + paramKeym + " and got " + size);
        for (Object paramValue : paramValues) {
            validateParam(type.items(), paramKeym, singletonList(paramValue), parameterType);
        }
    }

    private void validateUnion(UnionTypeDeclaration type, String paramKey, Collection<?> paramValues, String parameterType) throws Exception {
        StringBuilder message = new StringBuilder();

        for (TypeDeclaration unionComponent : type.of())
            try {
                unionComponent.type();
                validateParam(unionComponent, paramKey, paramValues, parameterType);
                return;
            } catch (Exception e) {
                message.append("- For ").append(paramKey).append(" one of the union component failed: ").append(e.getMessage()).append("\n");
            }

        throw new Exception(message.toString());
    }

    @Override
    public String message(String value)
    {
        List<ValidationResult> results = typeDeclaration.validate(value);
        return results.isEmpty() ? "OK" : results.get(0).getMessage();
    }

    private String message(TypeDeclaration type, String value)
    {
        List<ValidationResult> results = type.validate(value);
        return results.isEmpty() ? "OK" : results.get(0).getMessage();
    }

    @Override
    public boolean isRequired()
    {
        if (required == null) {
            required = typeDeclaration.required();
        }
        return required;
    }

    @Override
    public String getDefaultValue()
    {
        if (defaultValue == null) {
            defaultValue = fromNullable(typeDeclaration.defaultValue());
        }

        return defaultValue.orNull();
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
