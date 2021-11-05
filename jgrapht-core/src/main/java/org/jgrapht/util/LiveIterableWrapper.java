/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A wrapper around a supplier of an iterable.
 * 
 * @author Dimitrios Michail
 *
 * @param <E> the element type
 */
public class LiveIterableWrapper<E>
    implements
    Iterable<E>
{
    private Supplier<Iterable<E>> supplier;

    /**
     * Create a new wrapper
     */
    public LiveIterableWrapper()
    {
        this(null);
    }

    /**
     * Create a new wrapper
     * 
     * @param supplier the supplier which provides the iterable
     */
    public LiveIterableWrapper(Supplier<Iterable<E>> supplier)
    {
        this.supplier = Objects.requireNonNull(supplier);
    }

    @Override
    public Iterator<E> iterator()
    {
        return supplier.get().iterator();
    }

    /**
     * Get the supplier
     * 
     * @return the supplier
     */
    public Supplier<Iterable<E>> getSupplier()
    {
        return supplier;
    }

    /**
     * Set the supplier
     * 
     * @param supplier the supplier
     */
    public void setSupplier(Supplier<Iterable<E>> supplier)
    {
        this.supplier = supplier;
    }

}
