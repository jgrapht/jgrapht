/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
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

import java.util.*;

/**
 * Generates elements from the input collection in random order.
 * <p>
 * An element can be generated only once. After all elements have been generated, this generator
 * halts. At every step, an element is generated uniformly at random, which means that every element
 * has an equal probability to be generated. This implementation is based on the
 * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle"> Fisher-Yates algorithm</a>.
 * The generator is unbiased meaning the every permutation is equally likely.
 *
 * @param <T> element type
 * @author Timofey Chudakov
 */
public class ElementsSequenceGenerator<T>
    implements
    Iterator<T>,
    Iterable<T>
{

    /**
     * Input elements ordered as a list. This list is being decreased in size as the elements are
     * generated.
     */
    private List<T> elements;
    /**
     * Random instance used by this generator.
     */
    private Random rng;

    /**
     * Constructs a new {@link ElementsSequenceGenerator}.
     *
     * @param elements a collection of elements to generate elements from.
     */
    public ElementsSequenceGenerator(Collection<T> elements)
    {
        this(elements, System.nanoTime());
    }

    /**
     * Constructs a new {@link ElementsSequenceGenerator} using the specified {@code seed}. Two
     * different generators with the same seed will produce identical sequences given that the same
     * collection of elements is provided.
     *
     * @param elements a collection of elements to generate elements from.
     * @param seed a seed for the random number generator
     */
    public ElementsSequenceGenerator(Collection<T> elements, long seed)
    {
        this(elements, new Random(seed));
    }

    /**
     * Constructs a new {@link ElementsSequenceGenerator} using the specified random number
     * generator {@code rng}. Two different generators will produce identical sequences from a
     * collection of elements given that the random number generator produces the same sequence of
     * numbers.
     *
     * @param elements a collection of elements to generate elements from.
     * @param rng a random number generator
     */
    public ElementsSequenceGenerator(Collection<T> elements, Random rng)
    {
        this.elements = new ArrayList<>(elements);
        this.rng = rng;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext()
    {
        return !elements.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T next()
    {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int index = rng.nextInt(elements.size());
        T result = elements.get(index);

        elements.set(index, elements.get(elements.size() - 1));
        elements.remove(elements.size() - 1);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator()
    {
        return this;
    }
}
