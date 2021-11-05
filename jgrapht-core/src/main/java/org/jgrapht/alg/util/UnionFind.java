/*
 * (C) Copyright 2010-2021, by Tom Conerly and Contributors.
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
package org.jgrapht.alg.util;

import java.util.*;
import java.util.stream.*;

/**
 * An implementation of <a href="http://en.wikipedia.org/wiki/Disjoint-set_data_structure">Union
 * Find</a> data structure. Union Find is a disjoint-set data structure. It supports two operations:
 * finding the set a specific element is in, and merging two sets. The implementation uses union by
 * rank and path compression to achieve an amortized cost of $O(\alpha(n))$ per operation where
 * $\alpha$ is the inverse Ackermann function. UnionFind uses the hashCode and equals method of the
 * elements it operates on.
 *
 * @param <T> element type
 *
 * @author Tom Conerly
 */
public class UnionFind<T>
{
    private final Map<T, T> parentMap;
    private final Map<T, Integer> rankMap;
    private int count; // number of components

    /**
     * Creates a UnionFind instance with all the elements in separate sets.
     * 
     * @param elements the initial elements to include (each element in a singleton set).
     */
    public UnionFind(Set<T> elements)
    {
        parentMap = new LinkedHashMap<>();
        rankMap = new HashMap<>();
        for (T element : elements) {
            parentMap.put(element, element);
            rankMap.put(element, 0);
        }
        count = elements.size();
    }

    /**
     * Adds a new element to the data structure in its own set.
     *
     * @param element The element to add.
     */
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
     * @return map from element to parent element
     */
    protected Map<T, T> getParentMap()
    {
        return parentMap;
    }

    /**
     * @return map from element to rank
     */
    protected Map<T, Integer> getRankMap()
    {
        return rankMap;
    }

    /**
     * Returns the representative element of the set that element is in.
     *
     * @param element The element to find.
     *
     * @return The element representing the set the element is in.
     */
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
     * Merges the sets which contain element1 and element2. No guarantees are given as to which
     * element becomes the representative of the resulting (merged) set: this can be either
     * find(element1) or find(element2).
     *
     * @param element1 The first element to union.
     * @param element2 The second element to union.
     */
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
     * Tests whether two elements are contained in the same set.
     * 
     * @param element1 first element
     * @param element2 second element
     * @return true if element1 and element2 are contained in the same set, false otherwise.
     */
    public boolean inSameSet(T element1, T element2)
    {
        return find(element1).equals(find(element2));
    }

    /**
     * Returns the number of sets. Initially, all items are in their own set. The smallest number of
     * sets equals one.
     * 
     * @return the number of sets
     */
    public int numberOfSets()
    {
        assert count >= 1 && count <= parentMap.keySet().size();
        return count;
    }

    /**
     * Returns the total number of elements in this data structure.
     * 
     * @return the total number of elements in this data structure.
     */
    public int size()
    {
        return parentMap.size();
    }

    /**
     * Resets the UnionFind data structure: each element is placed in its own singleton set.
     */
    public void reset()
    {
        for (T element : parentMap.keySet()) {
            parentMap.put(element, element);
            rankMap.put(element, 0);
        }
        count = parentMap.size();
    }

    /**
     * Returns a string representation of this data structure. Each component is represented as
     * $\left{v_i:v_1,v_2,v_3,...v_n\right}$, where $v_i$ is the representative of the set.
     * 
     * @return string representation of this data structure
     */
    public String toString()
    {
        Map<T, Set<T>> setRep = new LinkedHashMap<>();
        for (T t : parentMap.keySet()) {
            T representative = find(t);
            if (!setRep.containsKey(representative))
                setRep.put(representative, new LinkedHashSet<>());
            setRep.get(representative).add(t);
        }

        return setRep
            .keySet().stream()
            .map(
                key -> "{" + key + ":"
                    + setRep
                        .get(key).stream().map(Objects::toString).collect(Collectors.joining(","))
                    + "}")
            .collect(Collectors.joining(", ", "{", "}"));
    }
}
