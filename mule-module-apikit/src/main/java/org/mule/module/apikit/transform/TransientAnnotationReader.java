/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.transform;

import com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

@SuppressWarnings("rawtypes")
public class TransientAnnotationReader extends AbstractInlineAnnotationReaderImpl<Type, Class, Field, Method>
        implements RuntimeAnnotationReader
{

    private static class XmlTransientProxyHandler implements InvocationHandler
    {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (args == null || args.length == 0)
            {
                if (method.getName().equals("annotationType"))
                {
                    return XmlTransient.class;
                }
                if (method.getName().equals("toString"))
                {
                    return "@XmlTransient";
                }
            }
            throw new UnsupportedOperationException("@XmlTransient doesn't support method call: " + method.getName());
        }

        private static XmlTransient create()
        {
            return (XmlTransient) Proxy.newProxyInstance(XmlTransientProxyHandler.class.getClassLoader(),
                                                         new Class[] {XmlTransient.class}, new XmlTransientProxyHandler());
        }
    }

    private static final Annotation XML_TRANSIENT_ANNOTATION = XmlTransientProxyHandler.create();
    private static final Annotation[] XML_TRANSIENT_ANNOTATION_ONLY = {XML_TRANSIENT_ANNOTATION};

    private final RuntimeInlineAnnotationReader delegate = new RuntimeInlineAnnotationReader();
    private final List<Class<?>> transientClasses = new ArrayList<Class<?>>();
    private final List<Field> transientFields = new ArrayList<Field>();
    private final List<Method> transientMethods = new ArrayList<Method>();

    public TransientAnnotationReader()
    {
    }

    // API

    public void addTransientClass(Class<?> cls)
    {
        transientClasses.add(cls);
    }

    public void addTransientField(Field field)
    {
        transientFields.add(field);
    }

    public void addTransientMethod(Method method)
    {
        transientMethods.add(method);
    }

    /// Classes

    @Override
    public boolean hasClassAnnotation(Class clazz, Class<? extends Annotation> annotationType)
    {
        if (transientClasses.contains(clazz))
        {
            return true;
        }
        return delegate.hasClassAnnotation(clazz, annotationType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getClassAnnotation(Class<A> annotationType, Class clazz, Locatable srcPos)
    {
        if (transientClasses.contains(clazz))
        {
            return (A) XML_TRANSIENT_ANNOTATION;
        }

        //return LocatableAnnotation.create(((Class<?>) clazz).getAnnotation(annotationType), srcPos);
        return delegate.getClassAnnotation(annotationType, clazz, srcPos);
    }

    /// Fields

    @Override
    public boolean hasFieldAnnotation(Class<? extends Annotation> annotationType, Field field)
    {
        if (XmlTransient.class.isAssignableFrom(annotationType))
        {
            if (transientFields.contains(field))
            {
                return true;
            }
        }
        return delegate.hasFieldAnnotation(annotationType, field);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getFieldAnnotation(Class<A> annotationType, Field field, Locatable srcPos)
    {
        if (XmlTransient.class.isAssignableFrom(annotationType))
        {
            if (transientFields.contains(field))
            {
                return (A) XML_TRANSIENT_ANNOTATION;
            }
        }
        return delegate.getFieldAnnotation(annotationType, field, srcPos);
    }

    @Override
    public Annotation[] getAllFieldAnnotations(Field field, Locatable srcPos)
    {
        if (transientFields.contains(field))
        {
            return XML_TRANSIENT_ANNOTATION_ONLY;
        }

        return delegate.getAllFieldAnnotations(field, srcPos);
    }

    /// Methods

    @Override
    public boolean hasMethodAnnotation(Class<? extends Annotation> annotationType, Method method)
    {
        if (XmlTransient.class.isAssignableFrom(annotationType))
        {
            if (transientMethods.contains(method))
            {
                return true;
            }

        }
        return delegate.hasMethodAnnotation(annotationType, method);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType, Method method, Locatable srcPos)
    {
        if (XmlTransient.class.isAssignableFrom(annotationType))
        {
            if (transientMethods.contains(method))
            {
                return (A) XML_TRANSIENT_ANNOTATION;
            }

        }
        return delegate.getMethodAnnotation(annotationType, method, srcPos);
    }

    @Override
    public Annotation[] getAllMethodAnnotations(Method method, Locatable srcPos)
    {
        if (transientMethods.contains(method))
        {
            return XML_TRANSIENT_ANNOTATION_ONLY;
        }

        return delegate.getAllMethodAnnotations(method, srcPos);
    }

    // default

    @Override
    public <A extends Annotation> A getMethodParameterAnnotation(Class<A> annotation, Method method, int paramIndex,
                                                                 Locatable srcPos)
    {
        return delegate.getMethodParameterAnnotation(annotation, method, paramIndex, srcPos);
    }

    @Override
    public <A extends Annotation> A getPackageAnnotation(Class<A> a, Class clazz, Locatable srcPos)
    {
        return delegate.getPackageAnnotation(a, clazz, srcPos);
    }

    @Override
    public Class getClassValue(Annotation a, String name)
    {
        return delegate.getClassValue(a, name);
    }

    @Override
    public Class[] getClassArrayValue(Annotation a, String name)
    {
        return delegate.getClassArrayValue(a, name);
    }

    @Override
    protected String fullName(Method m)
    {
        // same as RuntimeInlineAnnotationReader.fullName()
        return m.getDeclaringClass().getName() + '#' + m.getName();
    }

}
