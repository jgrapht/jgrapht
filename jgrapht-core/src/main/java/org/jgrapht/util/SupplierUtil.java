/*
 * (C) Copyright 2018-2020, by Dimitrios Michail and Contributors.
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
    public static final Supplier<DefaultEdge> DEFAULT_EDGE_SUPPLIER =
        createSupplier(DefaultEdge.class);

    /**
     * Supplier for {@link DefaultWeightedEdge}.
     */
    public static final Supplier<DefaultWeightedEdge> DEFAULT_WEIGHTED_EDGE_SUPPLIER =
        createSupplier(DefaultWeightedEdge.class);

    /**
     * Supplier for {@link Object}.
     */
    public static final Supplier<Object> OBJECT_SUPPLIER = createSupplier(Object.class);

    /**
     * Create a supplier from a class which calls the default constructor.
     * 
     * @param clazz the class
     * @return the supplier
     * @param <T> the type of results supplied by this supplier
     */
    public static <T> Supplier<T> createSupplier(Class<? extends T> clazz)
    {
        try {
            final Constructor<? extends T> constructor = clazz.getDeclaredConstructor();
            if ((!Modifier.isPublic(constructor.getModifiers())
                || !Modifier.isPublic(constructor.getDeclaringClass().getModifiers()))
                && !constructor.canAccess(null))
                constructor.setAccessible(true);
            return new ConstructorSupplier<>(constructor);
        } catch (ReflectiveOperationException e) {
            String error = e.getMessage();
            return new AlwaysFailingSupplier<T>(error);
        }
    }

    /**
     * Create a default edge supplier.
     * 
     * @return a default edge supplier
     */
    public static Supplier<DefaultEdge> createDefaultEdgeSupplier()
    {
        return createSupplier(DefaultEdge.class);
    }

    /**
     * Create a default weighted edge supplier.
     * 
     * @return a default weighted edge supplier
     */
    public static Supplier<DefaultWeightedEdge> createDefaultWeightedEdgeSupplier()
    {
        return createSupplier(DefaultWeightedEdge.class);
    }

    /**
     * Create an integer supplier which returns a sequence starting from zero.
     * 
     * @return an integer supplier
     */
    public static Supplier<Integer> createIntegerSupplier()
    {
        return new IntegerSupplier(0);
    }

    /**
     * Create an integer supplier which returns a sequence starting from a specific numbers.
     * 
     * @param start where to start the sequence
     * @return an integer supplier
     */
    public static Supplier<Integer> createIntegerSupplier(int start)
    {
        return new IntegerSupplier(start);
    }

    /**
     * Create a long supplier which returns a sequence starting from zero.
     * 
     * @return a long supplier
     */
    public static Supplier<Long> createLongSupplier()
    {
        return new LongSupplier(0);
    }

    /**
     * Create a long supplier which returns a sequence starting from a specific numbers.
     * 
     * @param start where to start the sequence
     * @return a long supplier
     */
    public static Supplier<Long> createLongSupplier(long start)
    {
        return new LongSupplier(start);
    }

    /**
     * Create a string supplier which returns unique strings. The returns strings are simply
     * integers starting from zero.
     * 
     * @return a string supplier
     */
    public static Supplier<String> createStringSupplier()
    {
        return new StringSupplier(0);
    }

    /**
     * Create a string supplier which returns random UUIDs.
     * 
     * @return a string supplier
     */
    public static Supplier<String> createRandomUUIDStringSupplier()
    {
        return new RandomUUIDStringSupplier();
    }

    /**
     * Create a string supplier which returns unique strings. The returns strings are simply
     * integers starting from start.
     * 
     * @param start where to start the sequence
     * @return a string supplier
     */
    public static Supplier<String> createStringSupplier(int start)
    {
        return new StringSupplier(start);
    }

    private static class IntegerSupplier
        implements
        Supplier<Integer>,
        Serializable
    {
        private static final long serialVersionUID = -4714266728630636497L;

        private int i = 0;

        public IntegerSupplier(int start)
        {
            this.i = start;
        }

        @Override
        public Integer get()
        {
            return i++;
        }
    }

    private static class LongSupplier
        implements
        Supplier<Long>,
        Serializable
    {
        private static final long serialVersionUID = 4994477932143967277L;

        private long i = 0;

        public LongSupplier(long start)
        {
            this.i = start;
        }

        @Override
        public Long get()
        {
            return i++;
        }
    }

    private static class StringSupplier
        implements
        Supplier<String>,
        Serializable
    {
        private static final long serialVersionUID = -5025488316341437260L;

        private int i = 0;

        public StringSupplier(int start)
        {
            this.i = start;
        }

        @Override
        public String get()
        {
            return String.valueOf(i++);
        }
    }

    private static class RandomUUIDStringSupplier
        implements
        Supplier<String>,
        Serializable
    {
        private static final long serialVersionUID = -4636552536822031851L;

        @Override
        public String get()
        {
            return UUID.randomUUID().toString();
        }
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
                    return new ConstructorSupplier<T>(type.getDeclaredConstructor());
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
                throw new RuntimeException("Supplier failed", ex);
            }
        }

        Object writeReplace()
            throws ObjectStreamException
        {
            return new SerializedForm<>(constructor.getDeclaringClass());
        }
    }

    private static class AlwaysFailingSupplier<T>
        implements
        Supplier<T>,
        Serializable
    {
        private static final long serialVersionUID = -560480634880773413L;

        private String error;

        public AlwaysFailingSupplier()
        {
        }

        public AlwaysFailingSupplier(String error)
        {
            this.error = error;
        }

        @Override
        public T get()
        {
            throw new RuntimeException(error);
        }
    }

}
