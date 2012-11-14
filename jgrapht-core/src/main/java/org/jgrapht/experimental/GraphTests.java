package org.jgrapht.experimental;

import java.util.*;

import org.jgrapht.*;


public final class GraphTests<V, E>
{
    //~ Constructors -----------------------------------------------------------

    private GraphTests()
    {
    }

    //~ Methods ----------------------------------------------------------------

    public static <V, E> boolean isEmpty(final Graph<V, E> g)
    {
        return g.edgeSet().isEmpty();
    }

    public static <V, E> boolean isComplete(final Graph<V, E> g)
    {
        final int n = g.vertexSet().size();
        return g.edgeSet().size()
            == n * (n - 1) / 2;
    }

    public static <V, E> boolean isConnected(final Graph<V, E> g)
    {
        final int numVertices = g.vertexSet().size();
        final int numEdges = g.edgeSet().size();

        if (numEdges < numVertices - 1) {
            return false;
        }
        if (numVertices < 2
            || numEdges > (numVertices - 1) * (numVertices - 2) / 2)
        {
            return true;
        }

        final Set<V> known = new HashSet<V>();
        final LinkedList<V> queue = new LinkedList<V>();
        V v = g.vertexSet().iterator().next();

        queue.add(v); // start with node 1
        known.add(v);

        while (!queue.isEmpty()) {
            v = queue.removeFirst();
            for (final V v1 : Graphs.neighborListOf(g, v)) {
                v = v1;
                if (!known.contains(v)) {
                    known.add(v);
                    queue.add(v);
                }
            }
        }
        return known.size() == numVertices;
    }

    public static <V, E> boolean isTree(final Graph<V, E> g)
    {
        return isConnected(g)
            && g.edgeSet().size() == g.vertexSet().size() - 1;
    }

    public static <V, E> boolean isBipartite(final Graph<V, E> g)
    {
        if (4 * g.edgeSet().size()
            > g.vertexSet().size() * g.vertexSet().size())
        {
            return false;
        }
        if (isEmpty(g)) {
            return true;
        }

        final Set<V> unknown = new HashSet<V>(g.vertexSet());
        final LinkedList<V> queue = new LinkedList<V>();
        V v = unknown.iterator().next();
        final Set<V> odd = new HashSet<V>();

        queue.add(v);

        while (!unknown.isEmpty()) {
            if (queue.isEmpty()) {
                queue.add(unknown.iterator().next());
            }

            v = queue.removeFirst();
            unknown.remove(v);

            for (final V n : Graphs.neighborListOf(g, v)) {
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
