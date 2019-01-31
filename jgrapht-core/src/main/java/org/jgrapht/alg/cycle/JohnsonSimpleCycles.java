/*
 * (C) Copyright 2013-2018, by Nikolay Ognyanov and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.builder.*;

import java.util.*;

/**
 * Find all simple cycles of a directed graph using the Johnson's algorithm.
 *
 * <p>
 * See:<br>
 * D.B.Johnson, Finding all the elementary circuits of a directed graph, SIAM J. Comput., 4 (1975),
 * pp. 77-84.
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 *
 * @author Nikolay Ognyanov
 */
public class JohnsonSimpleCycles<V, E>
    implements
    DirectedSimpleCycles<V, E>
{
    // The graph.
    private Graph<V, E> graph;

    // The main state of the algorithm.
    private List<List<V>> cycles = null;
    private V[] iToV = null;
    private Map<V, Integer> vToI = null;
    private Set<V> blocked = null;
    private Map<V, Set<V>> bSets = null;
    private ArrayDeque<V> stack = null;

    // The state of the embedded Tarjan SCC algorithm.
    private List<Set<V>> SCCs = null;
    private int index = 0;
    private Map<V, Integer> vIndex = null;
    private Map<V, Integer> vLowlink = null;
    private ArrayDeque<V> path = null;
    private Set<V> pathSet = null;

    /**
     * Create a simple cycle finder for the specified graph.
     *
     * @param graph - the DirectedGraph in which to find cycles.
     *
     * @throws IllegalArgumentException if the graph argument is <code>
     * null</code>.
     */
    public JohnsonSimpleCycles(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireDirected(graph, "Graph must be directed");
        if (GraphTests.hasMultipleEdges(graph)) {
            throw new IllegalArgumentException("Graph should not have multiple (parallel) edges");
        }
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

        int startIndex = 0;
        int size = graph.vertexSet().size();
        while (startIndex < size) {
            Pair<Graph<V, E>, Integer> minSCCGResult = findMinSCSG(startIndex);
            if (minSCCGResult != null) {
                startIndex = minSCCGResult.getSecond();
                Graph<V, E> scg = minSCCGResult.getFirst();
                V startV = toV(startIndex);
                for (E e : scg.outgoingEdgesOf(startV)) {
                    V v = graph.getEdgeTarget(e);
                    blocked.remove(v);
                    getBSet(v).clear();
                }
                findCyclesInSCG(startIndex, startIndex, scg);
                startIndex++;
            } else {
                break;
            }
        }

        List<List<V>> result = cycles;
        clearState();
        return result;
    }

    private Pair<Graph<V, E>, Integer> findMinSCSG(int startIndex)
    {
        /*
         * Per Johnson : "adjacency structure of strong component $K$ with least vertex in subgraph
         * of $G$ induced by $(s, s + 1, n)$". Or in contemporary terms: the strongly connected
         * component of the subgraph induced by $(v_1, \dotso ,v_n)$ which contains the minimum
         * (among those SCCs) vertex index. We return that index together with the graph.
         */
        initMinSCGState();

        List<Set<V>> SCCs = findSCCS(startIndex);

        // find the SCC with the minimum index
        int minIndexFound = Integer.MAX_VALUE;
        Set<V> minSCC = null;
        for (Set<V> scc : SCCs) {
            for (V v : scc) {
                int t = toI(v);
                if (t < minIndexFound) {
                    minIndexFound = t;
                    minSCC = scc;
                }
            }
        }
        if (minSCC == null) {
            return null;
        }

        // build a graph for the SCC found
        Graph<V,
            E> resultGraph = GraphTypeBuilder
                .<V, E> directed().edgeSupplier(graph.getEdgeSupplier())
                .vertexSupplier(graph.getVertexSupplier()).allowingMultipleEdges(false)
                .allowingSelfLoops(true).buildGraph();
        for (V v : minSCC) {
            resultGraph.addVertex(v);
        }
        for (V v : minSCC) {
            for (V w : minSCC) {
                E edge = graph.getEdge(v, w);
                if (edge != null) {
                    resultGraph.addEdge(v, w, edge);
                }
            }
        }

        Pair<Graph<V, E>, Integer> result = Pair.of(resultGraph, minIndexFound);
        clearMinSCCState();
        return result;
    }

    private List<Set<V>> findSCCS(int startIndex)
    {
        // Find SCCs in the subgraph induced
        // by vertices startIndex and beyond.
        // A call to StrongConnectivityAlgorithm
        // would be too expensive because of the
        // need to materialize the subgraph.
        // So - do a local search by the Tarjan's
        // algorithm and pretend that vertices
        // with an index smaller than startIndex
        // do not exist.
        for (V v : graph.vertexSet()) {
            int vI = toI(v);
            if (vI < startIndex) {
                continue;
            }
            if (!vIndex.containsKey(v)) {
                getSCCs(startIndex, vI);
            }
        }
        List<Set<V>> result = SCCs;
        SCCs = null;
        return result;
    }

    private void getSCCs(int startIndex, int vertexIndex)
    {
        V vertex = toV(vertexIndex);
        vIndex.put(vertex, index);
        vLowlink.put(vertex, index);
        index++;
        path.push(vertex);
        pathSet.add(vertex);

        Set<E> edges = graph.outgoingEdgesOf(vertex);
        for (E e : edges) {
            V successor = graph.getEdgeTarget(e);
            int successorIndex = toI(successor);
            if (successorIndex < startIndex) {
                continue;
            }
            if (!vIndex.containsKey(successor)) {
                getSCCs(startIndex, successorIndex);
                vLowlink.put(vertex, Math.min(vLowlink.get(vertex), vLowlink.get(successor)));
            } else if (pathSet.contains(successor)) {
                vLowlink.put(vertex, Math.min(vLowlink.get(vertex), vIndex.get(successor)));
            }
        }
        if (vLowlink.get(vertex).equals(vIndex.get(vertex))) {
            Set<V> result = new HashSet<>();
            V temp;
            do {
                temp = path.pop();
                pathSet.remove(temp);
                result.add(temp);
            } while (!vertex.equals(temp));
            if (result.size() == 1) {
                V v = result.iterator().next();
                if (graph.containsEdge(vertex, v)) {
                    SCCs.add(result);
                }
            } else {
                SCCs.add(result);
            }
        }
    }

    private boolean findCyclesInSCG(int startIndex, int vertexIndex, Graph<V, E> scg)
    {
        /*
         * Find cycles in a strongly connected graph per Johnson.
         */
        boolean foundCycle = false;
        V vertex = toV(vertexIndex);
        stack.push(vertex);
        blocked.add(vertex);

        for (E e : scg.outgoingEdgesOf(vertex)) {
            V successor = scg.getEdgeTarget(e);
            int successorIndex = toI(successor);
            if (successorIndex == startIndex) {
                List<V> cycle = new ArrayList<>(stack.size());
                stack.descendingIterator().forEachRemaining(cycle::add);
                cycles.add(cycle);
                foundCycle = true;
            } else if (!blocked.contains(successor)) {
                boolean gotCycle = findCyclesInSCG(startIndex, successorIndex, scg);
                foundCycle = foundCycle || gotCycle;
            }
        }
        if (foundCycle) {
            unblock(vertex);
        } else {
            for (E ew : scg.outgoingEdgesOf(vertex)) {
                V w = scg.getEdgeTarget(ew);
                Set<V> bSet = getBSet(w);
                bSet.add(vertex);
            }
        }
        stack.pop();
        return foundCycle;
    }

    private void unblock(V vertex)
    {
        blocked.remove(vertex);
        Set<V> bSet = getBSet(vertex);
        while (bSet.size() > 0) {
            V w = bSet.iterator().next();
            bSet.remove(w);
            if (blocked.contains(w)) {
                unblock(w);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initState()
    {
        cycles = new LinkedList<>();
        iToV = (V[]) graph.vertexSet().toArray();
        vToI = new HashMap<>();
        blocked = new HashSet<>();
        bSets = new HashMap<>();
        stack = new ArrayDeque<>();

        for (int i = 0; i < iToV.length; i++) {
            vToI.put(iToV[i], i);
        }
    }

    private void clearState()
    {
        cycles = null;
        iToV = null;
        vToI = null;
        blocked = null;
        bSets = null;
        stack = null;
    }

    private void initMinSCGState()
    {
        index = 0;
        SCCs = new ArrayList<>();
        vIndex = new HashMap<>();
        vLowlink = new HashMap<>();
        path = new ArrayDeque<>();
        pathSet = new HashSet<>();
    }

    private void clearMinSCCState()
    {
        index = 0;
        SCCs = null;
        vIndex = null;
        vLowlink = null;
        path = null;
        pathSet = null;
    }

    private Integer toI(V vertex)
    {
        return vToI.get(vertex);
    }

    private V toV(Integer i)
    {
        return iToV[i];
    }

    private Set<V> getBSet(V v)
    {
        // B sets typically not all needed,
        // so instantiate lazily.
        return bSets.computeIfAbsent(v, k -> new HashSet<>());
    }
}
