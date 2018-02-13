/*
 * (C) Copyright 2010-2018, by Tom Conerly and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.util;

import java.util.Objects;
import java.util.Set;

/**
 * An implementation of <a href="http://en.wikipedia.org/wiki/Disjoint-set_data_structure">Union
 * Find</a> data structure. Union Find is a disjoint-set data structure. It supports two operations:
 * finding the set a specific element is in, and merging two sets. The implementation uses union by
 * rank and path compression to achieve an amortized cost of O(a(n)) per operation where a is the
 * inverse Ackermann function. UnionFind uses the hashCode and equals method of the elements it
 * operates on.
 *
 * @param <T> element type
 *
 * @author Tom Conerly
 * @since Feb 10, 2010
 */
public class UnionFind<T> extends AbstractUnionFind<T> {
    /**
     * Creates a UnionFind instance with all the elements in separate sets.
     * 
     * @param elements the initial elements to include (each element in a singleton set).
     */
    public UnionFind(Set<T> elements)
    {
        super();

        if (Objects.nonNull(elements)){
            for (T element : elements) {
                parentMap.put(element, element);
                rankMap.put(element, 0);
            }
            count = elements.size();
        }
    }

    /**
     * Creates a UnionFind instance with no elements.
     */
    public UnionFind()
    {
        this(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addElement(T element)
    {
        if (parentMap.containsKey(element))
            throw new IllegalArgumentException(
                    "element is already contained in UnionFind: " + element);
        parentMap.put(element, element);
        rankMap.put(element, 0);
        count++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T find(final T element)
    {
        if (!parentMap.containsKey(element)) {
            throw new IllegalArgumentException(
                    "element is not contained in this UnionFind data structure: " + element);
        }

        T current = element;
        while (true) {
            T parent = parentMap.get(current);
            if (parent.equals(current)) {
                break;
            }
            current = parent;
        }
        final T root = current;

        current = element;
        while (!current.equals(root)) {
            T parent = parentMap.get(current);
            parentMap.put(current, root);
            current = parent;
        }

        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void union(T element1, T element2)
    {
        if (!parentMap.containsKey(element1) || !parentMap.containsKey(element2)) {
            throw new IllegalArgumentException("elements must be contained in given set");
        }

        T parent1 = find(element1);
        T parent2 = find(element2);

        // check if the elements are already in the same set
        if (parent1.equals(parent2)) {
            return;
        }

        int rank1 = rankMap.get(parent1);
        int rank2 = rankMap.get(parent2);
        if (rank1 > rank2) {
            parentMap.put(parent2, parent1);
        } else if (rank1 < rank2) {
            parentMap.put(parent1, parent2);
        } else {
            parentMap.put(parent2, parent1);
            rankMap.put(parent1, rank1 + 1);
        }
        count--;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset()
    {
        for (T element : parentMap.keySet()) {
            parentMap.put(element, element);
            rankMap.put(element, 0);
        }
        count = parentMap.size();
    }
}

// End UnionFind.java
