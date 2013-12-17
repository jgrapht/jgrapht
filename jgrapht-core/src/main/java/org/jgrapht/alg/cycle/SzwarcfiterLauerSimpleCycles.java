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
 * SzwarcfiterLauerSimpleCycles.java
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
import org.jgrapht.alg.*;


/**
 * Find all simple cycles of a directed graph using the Schwarcfiter and Lauer's
 * algorithm.
 *
 * <p/>See:<br/>
 * J.L.Szwarcfiter and P.E.Lauer, Finding the elementary cycles of a directed
 * graph in O(n + m) per cycle, Technical Report Series, #60, May 1974, Univ. of
 * Newcastle upon Tyne, Newcastle upon Tyne, England.
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 *
 * @author Nikolay Ognyanov
 */
public class SzwarcfiterLauerSimpleCycles<V, E>
    implements DirectedSimpleCycles<V, E>
{
    

    // The graph.
    private DirectedGraph<V, E> graph;

    // The state of the algorithm.
    private List<List<V>> cycles = null;
    private V [] iToV = null;
    private Map<V, Integer> vToI = null;
    private Map<V, Set<V>> bSets = null;
    private ArrayDeque<V> stack = null;
    private Set<V> marked = null;
    private Map<V, Set<V>> removed = null;
    private int [] position = null;
    private boolean [] reach = null;
    private List<V> startVertices = null;

    

    /**
     * Create a simple cycle finder with an unspecified graph.
     */
    public SzwarcfiterLauerSimpleCycles()
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
    public SzwarcfiterLauerSimpleCycles(DirectedGraph<V, E> graph)
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
        // Just a straightforward implementation of
        // the algorithm.
        if (graph == null) {
            throw new IllegalArgumentException("Null graph.");
        }
        initState();
        StrongConnectivityInspector<V, E> inspector =
            new StrongConnectivityInspector<V, E>(graph);
        List<Set<V>> sccs = inspector.stronglyConnectedSets();
        for (Set<V> scc : sccs) {
            int maxInDegree = -1;
            V startVertex = null;
            for (V v : scc) {
                int inDegree = graph.inDegreeOf(v);
                if (inDegree > maxInDegree) {
                    maxInDegree = inDegree;
                    startVertex = v;
                }
            }
            startVertices.add(startVertex);
        }

        for (V vertex : startVertices) {
            cycle(toI(vertex), 0);
        }

        List<List<V>> result = cycles;
        clearState();
        return result;
    }

    private boolean cycle(int v, int q)
    {
        boolean foundCycle = false;
        V vV = toV(v);
        marked.add(vV);
        stack.push(vV);
        int t = stack.size();
        position[v] = t;
        if (!reach[v]) {
            q = t;
        }
        Set<V> avRemoved = getRemoved(vV);
        Set<E> edgeSet = graph.outgoingEdgesOf(vV);
        Iterator<E> avIt = edgeSet.iterator();
        while (avIt.hasNext()) {
            E e = avIt.next();
            V wV = graph.getEdgeTarget(e);
            if (avRemoved.contains(wV)) {
                continue;
            }
            int w = toI(wV);
            if (!marked.contains(wV)) {
                boolean gotCycle = cycle(w, q);
                if (gotCycle) {
                    foundCycle = gotCycle;
                } else {
                    noCycle(v, w);
                }
            } else if (position[w] <= q) {
                foundCycle = true;
                List<V> cycle = new ArrayList<V>();
                Iterator<V> it = stack.descendingIterator();
                V current = null;
                while (it.hasNext()) {
                    current = it.next();
                    if (wV.equals(current)) {
                        break;
                    }
                }
                cycle.add(wV);
                while (it.hasNext()) {
                    current = it.next();
                    cycle.add(current);
                    if (current.equals(vV)) {
                        break;
                    }
                }
                cycles.add(cycle);
            } else {
                noCycle(v, w);
            }
        }
        stack.pop();
        if (foundCycle) {
            unmark(v);
        }
        reach[v] = true;
        position[v] = graph.vertexSet().size();
        return foundCycle;
    }

    private void noCycle(int x, int y)
    {
        V xV = toV(x);
        V yV = toV(y);

        Set<V> by = getBSet(yV);
        Set<V> axRemoved = getRemoved(xV);

        by.add(xV);
        axRemoved.add(yV);
    }

    private void unmark(int x)
    {
        V xV = toV(x);
        marked.remove(xV);
        Set<V> bx = getBSet(xV);
        for (V yV : bx) {
            Set<V> ayRemoved = getRemoved(yV);
            ayRemoved.remove(xV);
            if (marked.contains(yV)) {
                unmark(toI(yV));
            }
        }
        bx.clear();
    }

    @SuppressWarnings("unchecked")
    private void initState()
    {
        cycles = new ArrayList<List<V>>();
        iToV = (V []) graph.vertexSet().toArray();
        vToI = new HashMap<V, Integer>();
        bSets = new HashMap<V, Set<V>>();
        stack = new ArrayDeque<V>();
        marked = new HashSet<V>();
        removed = new HashMap<V, Set<V>>();
        int size = graph.vertexSet().size();
        position = new int[size];
        reach = new boolean[size];
        startVertices = new ArrayList<V>();

        for (int i = 0; i < iToV.length; i++) {
            vToI.put(iToV[i], i);
        }
    }

    private void clearState()
    {
        cycles = null;
        iToV = null;
        vToI = null;
        bSets = null;
        stack = null;
        marked = null;
        removed = null;
        position = null;
        reach = null;
        startVertices = null;
    }

    private Integer toI(V v)
    {
        return vToI.get(v);
    }

    private V toV(int i)
    {
        return iToV[i];
    }

    private Set<V> getBSet(V v)
    {
        // B sets are typically not all
        // needed, so instantiate lazily.
        Set<V> result = bSets.get(v);
        if (result == null) {
            result = new HashSet<V>();
            bSets.put(v, result);
        }
        return result;
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

// End SzwarcfiterLauerSimpleCycles.java
