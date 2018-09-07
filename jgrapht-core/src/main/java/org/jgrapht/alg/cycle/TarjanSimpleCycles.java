/*
 * (C) Copyright 2013-2018, by Nikolay Ognyanov and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;

import java.util.*;

/**
 * Find all simple cycles of a directed graph using the Tarjan's algorithm.
 *
 * <p>
 * See:<br>
 * R. Tarjan, Enumeration of the elementary circuits of a directed graph, SIAM J. Comput., 2 (1973),
 * pp. 211-216.
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 *
 * @author Nikolay Ognyanov
 */
public class TarjanSimpleCycles<V, E>
    implements
    DirectedSimpleCycles<V, E>
{
    private Graph<V, E> graph;

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
    public TarjanSimpleCycles(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireDirected(graph, "Graph must be directed");
    }

    /**
     * Get the graph
     * @return graph
     */
    public Graph<V, E> getGraph()
    {
        return graph;
    }

    /**
     * Set the graph
     * @param graph graph
     */
    public void setGraph(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireDirected(graph, "Graph must be directed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<V>> findSimpleCycles()
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
                List<V> cycle = new ArrayList<>();
                Iterator<V> it = pointStack.descendingIterator();
                V v;
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
        cycles = new ArrayList<>();
        marked = new HashSet<>();
        markedStack = new ArrayDeque<>();
        pointStack = new ArrayDeque<>();
        vToI = new HashMap<>();
        removed = new HashMap<>();
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
        return removed.computeIfAbsent(v, k -> new HashSet<>());
    }
}

// End TarjanSimpleCycles.java
