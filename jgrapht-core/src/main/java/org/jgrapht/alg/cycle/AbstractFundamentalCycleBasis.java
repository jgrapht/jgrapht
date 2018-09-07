/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;

import java.util.*;
import java.util.stream.*;

/**
 * A base implementation for the computation of a fundamental cycle basis of a graph. Subclasses
 * should only provide a method for constructing a spanning forest of the graph. A cycle basis is
 * fundamental if and only if each cycle in the basis contains at least one edge which is not
 * contained in any other cycle in the basis.
 * 
 * <p>
 * For information on algorithms and heuristics for the computation of fundamental cycle bases see
 * the following paper: Narsingh Deo, G. Prabhu, and M. S. Krishnamoorthy. Algorithms for Generating
 * Fundamental Cycles in a Graph. ACM Trans. Math. Softw. 8, 1, 26-42, 1982.
 * 
 * <p>
 * The implementation returns a fundamental cycle basis as an undirected cycle basis. For a
 * discussion of different kinds of cycle bases in graphs see the following paper: Christian
 * Liebchen, and Romeo Rizzi. Classes of Cycle Bases. Discrete Applied Mathematics, 155(3), 337-355,
 * 2007.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author Dimitrios Michail
 * @since October 2016
 */
public abstract class AbstractFundamentalCycleBasis<V, E>
    implements
    CycleBasisAlgorithm<V, E>
{
    protected Graph<V, E> graph;

    /**
     * Constructor
     * 
     * @param graph the input graph
     */
    public AbstractFundamentalCycleBasis(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireDirectedOrUndirected(graph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CycleBasis<V, E> getCycleBasis()
    {
        // compute spanning forest
        Map<V, E> spanningForest = computeSpanningForest();

        // collect set with all tree edges
        Set<E> treeEdges = spanningForest
            .entrySet().stream().map(Map.Entry::getValue).filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // build cycles for all non-tree edges
        Set<List<E>> cycles = new LinkedHashSet<>();
        int length = 0;
        double weight = 0d;
        for (E e : graph.edgeSet()) {
            if (!treeEdges.contains(e)) {
                Pair<List<E>, Double> c = buildFundamentalCycle(e, spanningForest);
                cycles.add(c.getFirst());
                length += c.getFirst().size();
                weight += c.getSecond();
            }
        }

        // return result
        return new CycleBasisImpl<>(graph, cycles, length, weight);
    }

    /**
     * Compute a spanning forest of the graph.
     * 
     * <p>
     * The representation assumes that the map contains the predecessor edge of each vertex in the
     * forest. The predecessor edge is the forest edge that was used to discover the vertex. If no
     * such edge was used (the vertex is a leaf in the forest) then the corresponding entry must be
     * null.
     * 
     * @return a map representation of a spanning forest.
     */
    protected abstract Map<V, E> computeSpanningForest();

    /**
     * Given a non-tree edge and a spanning tree (forest) build a fundamental cycle.
     * 
     * @param e a non-tree (forest) edge
     * @param spanningForest the spanning tree (forest)
     * @return a fundamental cycle
     */
    private Pair<List<E>, Double> buildFundamentalCycle(E e, Map<V, E> spanningForest)
    {
        V source = graph.getEdgeSource(e);
        V target = graph.getEdgeTarget(e);

        // handle self-loops
        if (source.equals(target)) {
            return Pair.of(Collections.singletonList(e), graph.getEdgeWeight(e));
        }

        /*
         * traverse half cycle
         */
        Set<E> path1 = new LinkedHashSet<>();
        path1.add(e);
        V cur = source;
        while (!cur.equals(target)) {
            E edgeToParent = spanningForest.get(cur);
            if (edgeToParent == null) {
                break;
            }
            V parent = Graphs.getOppositeVertex(graph, edgeToParent, cur);
            path1.add(edgeToParent);
            cur = parent;
        }

        /*
         * traverse the other half cycle, while removing common edges
         */
        double path2Weight = 0d;
        LinkedList<E> path2 = new LinkedList<>();
        if (!cur.equals(target)) {
            cur = target;
            while (true) {
                E edgeToParent = spanningForest.get(cur);
                if (edgeToParent == null) {
                    break;
                }
                V parent = Graphs.getOppositeVertex(graph, edgeToParent, cur);
                if (path1.contains(edgeToParent)) {
                    path1.remove(edgeToParent);
                } else {
                    path2.add(edgeToParent);
                    path2Weight += graph.getEdgeWeight(edgeToParent);
                }
                cur = parent;
            }
        }

        // now build cycle
        for (E a : path1) {
            path2Weight += graph.getEdgeWeight(a);
            path2.addFirst(a);
        }

        return Pair.of(path2, path2Weight);
    }

}

// End AbstractFundamentalCycleBasis.java
