/*
 * (C) Copyright 2018-2018, by Nikhil Sharma and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.Graph;

import java.util.*;

/**
 * Generator which produces the
 * <a href="http://mathworld.wolfram.com/LineGraph.html">line graph</a> of a given input
 * graph.
 * The line graph of an undirected graph G is another graph L(G) that represents the
 * adjacencies between edges of G.
 * The line graph of a directed graph G is the directed graph L(G) whose vertex set
 * corresponds to the arc set of G and having an arc directed from an edge e_1 to an
 * edge e_2 if in G, the head of e_1 meets the tail of e_2
 *
 * The runtime complexity is $O(|V|^2)$.
 *
 * <p>
 * More formally, let $G = (V, E)$ be a graph then its line graph L(G) is such that
 * - each vertex of L(G) represents an edge of G; and
 * - two vertices of L(G) are adjacent if and only if their corresponding edges share
 *   a common endpoint ("are incident") in G.
 * <p>
 *
 * @author Nikhil Sharma
 * @since June 2018
 *
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LineGraphGenerator <V, E>
        implements
        GraphGenerator<V, E, V>
{

    private final Graph<V, E> graph;

    /**
     * Line Graph Generator
     *
     * @param graph input graph
     */
    public LineGraphGenerator(Graph<V, E> graph)
    {
        this.graph = graph;
    }


    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        if (graph.getType().isDirected()) {
            List<E> edgeList = new ArrayList<>(graph.edgeSet());

            for (E edge1 : edgeList) {
                for (E edge2 : edgeList) {
                    if (edge1 == edge2) {
                        continue;
                    }

                    if (graph.getEdgeTarget(edge1) == graph.getEdgeSource(edge2)) {
                        target.addVertex((V) edge1);
                        target.addVertex((V) edge2);
                        target.addEdge((V) edge1, (V) edge2);
                    }
                }
            }
        } else{ // undirected graph
            List<E> edgeList = new ArrayList<>(graph.edgeSet());

            for (E edge1 : edgeList) {
                for (E edge2 : edgeList) {
                    if (edge1 == edge2) {
                        continue;
                    }

                    if (graph.getEdgeTarget(edge1) == graph.getEdgeSource(edge2)
                            || graph.getEdgeTarget(edge1) == graph.getEdgeTarget(edge2)
                            || graph.getEdgeSource(edge1) == graph.getEdgeSource(edge2)
                            || graph.getEdgeSource(edge1) == graph.getEdgeTarget(edge2)) {
                        target.addVertex((V) edge1);
                        target.addVertex((V) edge2);
                        target.addEdge((V) edge1, (V) edge2);
                    }
                }
            }
        }
    }
}
