/* This program and the accompanying materials are dual-licensed under
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
package org.jgrapht.experimental;

import java.util.*;

import org.jgrapht.*;


public final class GraphTests<V, E>
{
    

    private GraphTests()
    {
    }

    

    public static <V, E> boolean isEmpty(Graph<V, E> g)
    {
        return g.edgeSet().isEmpty();
    }

    public static <V, E> boolean isComplete(Graph<V, E> g)
    {
        int n = g.vertexSet().size();
        return g.edgeSet().size()
            == (n * (n - 1) / 2);
    }

    public static <V, E> boolean isConnected(Graph<V, E> g)
    {
        int numVertices = g.vertexSet().size();
        int numEdges = g.edgeSet().size();

        if (numEdges < (numVertices - 1)) {
            return false;
        }
        if ((numVertices < 2)
            || (numEdges > ((numVertices - 1) * (numVertices - 2) / 2)))
        {
            return true;
        }

        Set<V> known = new HashSet<V>();
        LinkedList<V> queue = new LinkedList<V>();
        V v = g.vertexSet().iterator().next();

        queue.add(v); // start with node 1
        known.add(v);

        while (!queue.isEmpty()) {
            v = queue.removeFirst();
            for (
                Iterator<V> it = Graphs.neighborListOf(g, v).iterator();
                it.hasNext();)
            {
                v = it.next();
                if (!known.contains(v)) {
                    known.add(v);
                    queue.add(v);
                }
            }
        }
        return known.size() == numVertices;
    }

    public static <V, E> boolean isTree(Graph<V, E> g)
    {
        return isConnected(g)
            && (g.edgeSet().size() == (g.vertexSet().size() - 1));
    }

    public static <V, E> boolean isBipartite(Graph<V, E> g)
    {
        if ((4 * g.edgeSet().size())
            > (g.vertexSet().size() * g.vertexSet().size()))
        {
            return false;
        }
        if (isEmpty(g)) {
            return true;
        }

        Set<V> unknown = new HashSet<V>(g.vertexSet());
        LinkedList<V> queue = new LinkedList<V>();
        V v = unknown.iterator().next();
        Set<V> odd = new HashSet<V>();

        queue.add(v);

        while (!unknown.isEmpty()) {
            if (queue.isEmpty()) {
                queue.add(unknown.iterator().next());
            }

            v = queue.removeFirst();
            unknown.remove(v);

            for (
                Iterator<V> it = Graphs.neighborListOf(g, v).iterator();
                it.hasNext();)
            {
                V n = it.next();
                if (unknown.contains(n)) {
                    queue.add(n);
                    if (!odd.contains(v)) {
                        odd.add(n);
                    }
                } else if (!(odd.contains(v) ^ odd.contains(n))) {
                    return false;
                }
            }
        }
        return true;
    }
}

// End GraphTests.java
