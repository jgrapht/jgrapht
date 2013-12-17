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
 * TarjanSimpleCycles.java
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
 * Find all simple cycles of a directed graph using the Tarjan's algorithm.
 *
 * <p/>See:<br/>
 * R. Tarjan, Enumeration of the elementary circuits of a directed graph, SIAM
 * J. Comput., 2 (1973), pp. 211-216.
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 *
 * @author Nikolay Ognyanov
 */
public class TarjanSimpleCycles<V, E>
    implements DirectedSimpleCycles<V, E>
{
    

    private DirectedGraph<V, E> graph;

    private List<List<V>> cycles;
    private Set<V> marked;
    private ArrayDeque<V> markedStack;
    private ArrayDeque<V> pointStack;
    private Map<V, Integer> vToI;
    private Map<V, Set<V>> removed;

    

    /**
     * Create a simple cycle finder with an unspecified graph.
     */
    public TarjanSimpleCycles()
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
    public TarjanSimpleCycles(DirectedGraph<V, E> graph)
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
        initState();

        for (V start : graph.vertexSet()) {
            backtrack(start, start);
            while (!markedStack.isEmpty()) {
                marked.remove(markedStack.pop());
            }
        }

        List<List<V>> result = cycles;
        clearState();
        return result;
    }

    private boolean backtrack(V start, V vertex)
    {
        boolean foundCycle = false;
        pointStack.push(vertex);
        marked.add(vertex);
        markedStack.push(vertex);

        for (E currentEdge : graph.outgoingEdgesOf(vertex)) {
            V currentVertex = graph.getEdgeTarget(currentEdge);
            if (getRemoved(vertex).contains(currentVertex)) {
                continue;
            }
            int comparison = toI(currentVertex).compareTo(toI(start));
            if (comparison < 0) {
                getRemoved(vertex).add(currentVertex);
            } else if (comparison == 0) {
                foundCycle = true;
                List<V> cycle = new ArrayList<V>();
                Iterator<V> it = pointStack.descendingIterator();
                V v = null;
                while (it.hasNext()) {
                    v = it.next();
                    if (start.equals(v)) {
                        break;
                    }
                }
                cycle.add(start);
                while (it.hasNext()) {
                    cycle.add(it.next());
                }
                cycles.add(cycle);
            } else if (!marked.contains(currentVertex)) {
                boolean gotCycle = backtrack(start, currentVertex);
                foundCycle = foundCycle || gotCycle;
            }
        }

        if (foundCycle) {
            while (!markedStack.peek().equals(vertex)) {
                marked.remove(markedStack.pop());
            }
            marked.remove(markedStack.pop());
        }

        pointStack.pop();
        return foundCycle;
    }

    private void initState()
    {
        cycles = new ArrayList<List<V>>();
        marked = new HashSet<V>();
        markedStack = new ArrayDeque<V>();
        pointStack = new ArrayDeque<V>();
        vToI = new HashMap<V, Integer>();
        removed = new HashMap<V, Set<V>>();
        int index = 0;
        for (V v : graph.vertexSet()) {
            vToI.put(v, index++);
        }
    }

    private void clearState()
    {
        cycles = null;
        marked = null;
        markedStack = null;
        pointStack = null;
        vToI = null;
    }

    private Integer toI(V v)
    {
        return vToI.get(v);
    }

    private Set<V> getRemoved(V v)
    {
        // Removed sets typically not all
        // needed, so instantiate lazily.
        Set<V> result = removed.get(v);
        if (result == null) {
            result = new HashSet<V>();
            removed.put(v, result);
        }
        return result;
    }
}

// End TarjanSimpleCycles.java
