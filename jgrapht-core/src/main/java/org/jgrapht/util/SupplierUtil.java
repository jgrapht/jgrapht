/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.util;

import org.jgrapht.graph.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/**
 * Helper class for suppliers.
 *
 * @author Dimitrios Michail
 */
public class SupplierUtil
{

    /**
     * Supplier for {@link DefaultEdge}.
     */
    @SuppressWarnings("unchecked")
    public static final Supplier<DefaultEdge> DEFAULT_EDGE_SUPPLIER =
        (Supplier<DefaultEdge> & Serializable) DefaultEdge::new;

    /**
     * Supplier for {@link DefaultWeightedEdge}.
     */
    @SuppressWarnings("unchecked")
    public static final Supplier<DefaultWeightedEdge> DEFAULT_WEIGHTED_EDGE_SUPPLIER =
        (Supplier<DefaultWeightedEdge> & Serializable) DefaultWeightedEdge::new;

    /**
     * Supplier for {@link Object}.
     */
    @SuppressWarnings("unchecked")
    public static final Supplier<Object> OBJECT_SUPPLIER =
        (Supplier<Object> & Serializable) Object::new;

    /**
     * Create a supplier from a class which calls the default constructor.
     * 
     * @param clazz the class
     * @return the supplier
     * @param <T> the type of results supplied by this supplier
     */
    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> createSupplier(Class<? extends T> clazz)
    {
        // shortcut to use pre-defined constructor method reference based suppliers
        if (clazz == DefaultEdge.class) {
            return (Supplier<T>) DEFAULT_EDGE_SUPPLIER;
        } else if (clazz == DefaultWeightedEdge.class) {
            return (Supplier<T>) DEFAULT_WEIGHTED_EDGE_SUPPLIER;
        } else if (clazz == Object.class) {
            return (Supplier<T>) OBJECT_SUPPLIER;
        }

        try {
            final Constructor<? extends T> constructor = clazz.getDeclaredConstructor();
            if ((!Modifier.isPublic(constructor.getModifiers())
                || !Modifier.isPublic(constructor.getDeclaringClass().getModifiers()))
                && !constructor.canAccess(null))
            {
                constructor.setAccessible(true);
            }
            return new ConstructorSupplier<>(constructor);
        } catch (ReflectiveOperationException e) {
            // Defer throwing an exception to the first time the supplier is called
            return getThrowingSupplier(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Supplier<T> getThrowingSupplier(Throwable e)
    {
        return (Supplier<T> & Serializable) () -> {
            throw new SupplierException(e.getMessage(), e);
        };
    }

    /**
     * Create a default edge supplier.
     * 
     * @return a default edge supplier
     */
    public static Supplier<DefaultEdge> createDefaultEdgeSupplier()
    {
        return DEFAULT_EDGE_SUPPLIER;
    }

    /**
     * Create a default weighted edge supplier.
     * 
     * @return a default weighted edge supplier
     */
    public static Supplier<DefaultWeightedEdge> createDefaultWeightedEdgeSupplier()
    {
        return DEFAULT_WEIGHTED_EDGE_SUPPLIER;
    }

    /**
     * Create an integer supplier which returns a sequence starting from zero.
     * 
     * @return an integer supplier
     */
    public static Supplier<Integer> createIntegerSupplier()
    {
        return createIntegerSupplier(0);
    }

    /**
     * Create an integer supplier which returns a sequence starting from a specific numbers.
     * 
     * @param start where to start the sequence
     * @return an integer supplier
     */
    @SuppressWarnings("unchecked")
    public static Supplier<Integer> createIntegerSupplier(int start)
    {
        int[] modifiableInt = new int[] { start }; // like a modifiable int
        return (Supplier<Integer> & Serializable) () -> modifiableInt[0]++;
    }

    /**
     * Create a long supplier which returns a sequence starting from zero.
     * 
     * @return a long supplier
     */
    public static Supplier<Long> createLongSupplier()
    {
        return createLongSupplier(0);
    }

    /**
     * Create a long supplier which returns a sequence starting from a specific numbers.
     * 
     * @param start where to start the sequence
     * @return a long supplier
     */
    @SuppressWarnings("unchecked")
    public static Supplier<Long> createLongSupplier(long start)
    {
        long[] modifiableLong = new long[] { start }; // like a modifiable long
        return (Supplier<Long> & Serializable) () -> modifiableLong[0]++;
    }

    /**
     * Create a string supplier which returns unique strings. The returns strings are simply
     * integers starting from zero.
     * 
     * @return a string supplier
     */
    public static Supplier<String> createStringSupplier()
    {
        return createStringSupplier(0);
    }

    /**
     * Create a string supplier which returns random UUIDs.
     * 
     * @return a string supplier
     */
    @SuppressWarnings("unchecked")
    public static Supplier<String> createRandomUUIDStringSupplier()
    {
        return (Supplier<String> & Serializable) () -> UUID.randomUUID().toString();
    }

    /**
     * Create a string supplier which returns unique strings. The returns strings are simply
     * integers starting from start.
     * 
     * @param start where to start the sequence
     * @return a string supplier
     */
    @SuppressWarnings("unchecked")
    public static Supplier<String> createStringSupplier(int start)
    {
        int[] container = new int[] { start };
        return (Supplier<String> & Serializable) () -> String.valueOf(container[0]++);
    }

    private static class ConstructorSupplier<T>
        implements
        Supplier<T>,
        Serializable
    {
        private final Constructor<? extends T> constructor;

        private static class SerializedForm<T>
            implements
            Serializable
        {
            private static final long serialVersionUID = -2385289829144892760L;

            private final Class<T> type;

            public SerializedForm(Class<T> type)
            {
                this.type = type;
            }

            Object readResolve()
                throws ObjectStreamException
            {
                try {
                    return new ConstructorSupplier<>(type.getDeclaredConstructor());
                } catch (ReflectiveOperationException e) {
                    InvalidObjectException ex = new InvalidObjectException(
                        "Failed to get no-args constructor from " + type);
                    ex.initCause(e);
                    throw ex;
                }
            }
        }

        public ConstructorSupplier(Constructor<? extends T> constructor)
        {
            this.constructor = constructor;
        }

        @Override
        public T get()
        {
            try {
                return constructor.newInstance();
            } catch (ReflectiveOperationException ex) {
                throw new SupplierException("Supplier failed", ex);
            }
        }

        Object writeReplace()
            throws ObjectStreamException
        {
            return new SerializedForm<>(constructor.getDeclaringClass());
        }
    }
}
