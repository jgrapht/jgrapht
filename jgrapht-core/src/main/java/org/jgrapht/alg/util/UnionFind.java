/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2010, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* -------------------------
 * UnionFind.java
 * -------------------------
 * (C) Copyright 2010-2010, by Tom Conerly and Contributors.
 *
 * Original Author:  Tom Conerly
 * Contributor(s):
 *
 * Changes
 * -------
 * 02-Feb-2010 : Initial revision (TC);
 *
 */
package org.jgrapht.alg.util;

import com.google.common.collect.Maps;

import java.util.*;


/**
 * An implementation of <a
 * href="http://en.wikipedia.org/wiki/Disjoint-set_data_structure">Union
 * Find</a> data structure. Union Find is a disjoint-set data structure. It
 * supports two operations: finding the set a specific element is in, and
 * merging two sets. The implementation uses union by rank and path compression
 * to achieve an amortized cost of O(a(n)) per operation where a is the inverse
 * Ackermann function. UnionFind uses the hashCode and equals method of the
 * elements it operates on.
 *
 * @author Tom Conerly
 * @since Feb 10, 2010
 */
public class UnionFind<T>
{
    //~ Instance fields --------------------------------------------------------

    private final Map<T, T> parentMap;
    private final Map<T, Integer> rankMap;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a UnionFind instance with all of the elements of elements in
     * seperate sets.
     */
    public UnionFind(final Set<T> elements)
    {
        parentMap = Maps.newHashMap();
        rankMap = Maps.newHashMap();
        for (final T element : elements) {
            parentMap.put(element, element);
            rankMap.put(element, 0);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Adds a new element to the data structure in its own set.
     *
     * @param element The element to add.
     */
    public void addElement(final T element)
    {
        parentMap.put(element, element);
        rankMap.put(element, 0);
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
                "elements must be contained in given set");
        }

        final T parent = parentMap.get(element);
        if (parent.equals(element)) {
            return element;
        }

        final T newParent = find(parent);
        parentMap.put(element, newParent);
        return newParent;
    }

    /**
     * Merges the sets which contain element1 and element2.
     *
     * @param element1 The first element to union.
     * @param element2 The second element to union.
     */
    public void union(final T element1, final T element2)
    {
        if (!parentMap.containsKey(element1)
            || !parentMap.containsKey(element2))
        {
            throw new IllegalArgumentException(
                "elements must be contained in given set");
        }

        final T parent1 = find(element1);
        final T parent2 = find(element2);

        //check if the elements are already in the same set
        if (parent1.equals(parent2)) {
            return;
        }

        final int rank1 = rankMap.get(parent1);
        final int rank2 = rankMap.get(parent2);
        if (rank1 > rank2) {
            parentMap.put(parent2, parent1);
        } else if (rank1 < rank2) {
            parentMap.put(parent1, parent2);
        } else {
            parentMap.put(parent2, parent1);
            rankMap.put(parent1, rank1 + 1);
        }
    }
}

// End UnionFind.java
