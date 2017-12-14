/*
 * (C) Copyright 2017-2017, by Alexandru Valeanu and Contributors.
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * A skeletal implementation of a data structure that keeps track of a set of
 * elements partitioned into a number of disjoint (non-overlapping) subsets.
 *
 * @param <T> element type
 *
 * @author Alexandru Valeanu
 */
public abstract class AbstractUnionFind<T> {
    protected final Map<T, T> parentMap;
    protected final Map<T, Integer> rankMap;
    protected int count; // number of components

    /**
     * Create a new UnionFind instance.
     */
    protected AbstractUnionFind() {
        parentMap = new HashMap<>();
        rankMap = new HashMap<>();
        count = 0;
    }

    /**
     * Adds a new element to the data structure in its own set.
     *
     * @param element The element to add.
     */
    public abstract void addElement(T element);

    /**
     * Returns the representative element of the set that element is in.
     *
     * @param element The element to find.
     *
     * @return The element representing the set the element is in.
     */
    public abstract T find(final T element);

    /**
     * Merges the sets which contain element1 and element2. No guarantees are given as to which
     * element becomes the representative of the resulting (merged) set: this can be either
     * find(element1) or find(element2).
     *
     * @param element1 The first element to union.
     * @param element2 The second element to union.
     */
    public abstract void union(T element1, T element2);

    /**
     * Resets the UnionFind data structure: each element is placed in its own singleton set.
     */
    public abstract void reset();

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
     * Returns a string representation of this data structure. Each component is represented as
     * {v_i:v_1,v_2,v_3,...v_n}, where v_i is the representative of the set.
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
                        key -> "{" + key + ":" + setRep.get(key).stream().map(Objects::toString).collect(
                                Collectors.joining(",")) + "}")
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
