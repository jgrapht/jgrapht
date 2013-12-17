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
 * PatonCycleBase.java
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
 * Find a cycle base of an undirected graph using the Paton's algorithm.
 *
 * <p/>See:<br/>
 * K. Paton, An algorithm for finding a fundamental set of cycles for an
 * undirected linear graph, Comm. ACM 12 (1969), pp. 514-518.
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 *
 * @author Nikolay Ognyanov
 */
public class PatonCycleBase<V, E>
    implements UndirectedCycleBase<V, E>
{
    

    private UndirectedGraph<V, E> graph;

    

    /**
     * Create a cycle base finder with an unspecified graph.
     */
    public PatonCycleBase()
    {
    }

    /**
     * Create a cycle base finder for the specified graph.
     *
     * @param graph - the DirectedGraph in which to find cycles.
     *
     * @throws IllegalArgumentException if the graph argument is <code>
     * null</code>.
     */
    public PatonCycleBase(UndirectedGraph<V, E> graph)
    {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph argument.");
        }
        this.graph = graph;
    }

    

    /**
     * {@inheritDoc}
     */
    @Override public UndirectedGraph<V, E> getGraph()
    {
        return graph;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void setGraph(UndirectedGraph<V, E> graph)
    {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph argument.");
        }
        this.graph = graph;
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<List<V>> findCycleBase()
    {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph.");
        }
        Map<V, Set<V>> used = new HashMap<V, Set<V>>();
        Map<V, V> parent = new HashMap<V, V>();
        ArrayDeque<V> stack = new ArrayDeque<V>();
        List<List<V>> cycles = new ArrayList<List<V>>();

        for (V root : graph.vertexSet()) {
            // Loop over the connected
            // components of the graph.
            if (parent.containsKey(root)) {
                continue;
            }

            // Free some memory in case of
            // multiple connected components.
            used.clear();

            // Prepare to walk the spanning tree.
            parent.put(root, root);
            used.put(root, new HashSet<V>());
            stack.push(root);

            // Do the walk. It is a BFS with
            // a LIFO instead of the usual
            // FIFO. Thus it is easier to
            // find the cycles in the tree.
            while (!stack.isEmpty()) {
                V current = stack.pop();
                Set<V> currentUsed = used.get(current);
                for (E e : graph.edgesOf(current)) {
                    V neighbor = graph.getEdgeTarget(e);
                    if (neighbor.equals(current)) {
                        neighbor = graph.getEdgeSource(e);
                    }
                    if (!used.containsKey(neighbor)) {
                        // found a new node
                        parent.put(neighbor, current);
                        Set<V> neighbourUsed = new HashSet<V>();
                        neighbourUsed.add(current);
                        used.put(neighbor, neighbourUsed);
                        stack.push(neighbor);
                    } else if (neighbor.equals(current)) {
                        // found a self loop
                        List<V> cycle = new ArrayList<V>();
                        cycle.add(current);
                        cycles.add(cycle);
                    } else if (!currentUsed.contains(neighbor)) {
                        // found a cycle
                        Set<V> neighbourUsed = used.get(neighbor);
                        List<V> cycle = new ArrayList<V>();
                        cycle.add(neighbor);
                        cycle.add(current);
                        V p = parent.get(current);
                        while (!neighbourUsed.contains(p)) {
                            cycle.add(p);
                            p = parent.get(p);
                        }
                        cycle.add(p);
                        cycles.add(cycle);
                        neighbourUsed.add(current);
                    }
                }
            }
        }
        return cycles;
    }
}

// End PatonCycleBase.java
