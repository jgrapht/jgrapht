/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -------------------------
 * TiernanSimpleCycles.java
 * -------------------------
 * (C) Copyright 2013, by Nikolay Ognyanov
 *
 * Original Author: Nikolay Ognyanov
 * Contributor(s) :
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Sep-2013 : Initial revision (NO);
 */
package org.jgrapht.alg.cycle;

import java.util.*;

import org.jgrapht.*;


/**
 * Find all simple cycles of a directed graph using the Tiernan's algorithm.
 *
 * <p/>See:<br/>
 * J.C.Tiernan An Efficient Search Algorithm Find the Elementary Circuits of a
 * Graph., Communications of the ACM, vol.13, 12, (1970), pp. 722 - 726.
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 *
 * @author Nikolay Ognyanov
 */
public class TiernanSimpleCycles<V, E>
    implements DirectedSimpleCycles<V, E>
{
    

    private DirectedGraph<V, E> graph;

    

    /**
     * Create a simple cycle finder with an unspecified graph.
     */
    public TiernanSimpleCycles()
    {
    }

    /**
     * Create a simple cycle finder for the specified graph.
     *
     * @param graph - the DirectedGraph in which to find cycles.
     *
     * @throws IllegalArgumentException if the graph argument is <code>
     * null</code>.
     */
    public TiernanSimpleCycles(DirectedGraph<V, E> graph)
    {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph argument.");
        }
        this.graph = graph;
    }

    

    /**
     * {@inheritDoc}
     */
    @Override public DirectedGraph<V, E> getGraph()
    {
        return graph;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void setGraph(DirectedGraph<V, E> graph)
    {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph argument.");
        }
        this.graph = graph;
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<List<V>> findSimpleCycles()
    {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph.");
        }
        Map<V, Integer> indices = new HashMap<V, Integer>();
        List<V> path = new ArrayList<V>();
        Set<V> pathSet = new HashSet<V>();
        Map<V, Set<V>> blocked = new HashMap<V, Set<V>>();
        List<List<V>> cycles = new LinkedList<List<V>>();

        int index = 0;
        for (V v : graph.vertexSet()) {
            blocked.put(v, new HashSet<V>());
            indices.put(v, index++);
        }

        Iterator<V> vertexIterator = graph.vertexSet().iterator();
        if (!vertexIterator.hasNext()) {
            return cycles;
        }

        V startOfPath = null;
        V endOfPath = null;
        V temp = null;
        int endIndex = 0;
        boolean extensionFound = false;

        endOfPath = vertexIterator.next();
        path.add(endOfPath);
        pathSet.add(endOfPath);

        // A mostly straightforward implementation
        // of the algorithm. Except that there is
        // no real need for the state machine from
        // the original paper.
        while (true) {
            // path extension
            do {
                extensionFound = false;
                for (E e : graph.outgoingEdgesOf(endOfPath)) {
                    V n = graph.getEdgeTarget(e);
                    int cmp =
                        indices.get(n).compareTo(indices.get(path.get(0)));
                    if ((cmp > 0)
                        && !pathSet.contains(n)
                        && !blocked.get(endOfPath).contains(n))
                    {
                        path.add(n);
                        pathSet.add(n);
                        endOfPath = n;
                        extensionFound = true;
                        break;
                    }
                }
            } while (extensionFound);

            // circuit confirmation
            startOfPath = path.get(0);
            if (graph.containsEdge(endOfPath, startOfPath)) {
                List<V> cycle = new ArrayList<V>();
                cycle.addAll(path);
                cycles.add(cycle);
            }

            // vertex closure
            if (path.size() > 1) {
                blocked.get(endOfPath).clear();
                endIndex = path.size() - 1;
                path.remove(endIndex);
                pathSet.remove(endOfPath);
                --endIndex;
                temp = endOfPath;
                endOfPath = path.get(endIndex);
                blocked.get(endOfPath).add(temp);
                continue;
            }

            // advance initial index
            if (vertexIterator.hasNext()) {
                path.clear();
                pathSet.clear();
                endOfPath = vertexIterator.next();
                path.add(endOfPath);
                pathSet.add(endOfPath);
                for (V vt : blocked.keySet()) {
                    blocked.get(vt).clear();
                }
                continue;
            }

            // terminate
            break;
        }

        return cycles;
    }
}

// End TiernanSimpleCycles.java
