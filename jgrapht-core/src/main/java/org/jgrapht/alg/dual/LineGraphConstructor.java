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
package org.jgrapht.alg.dual;

import org.jgrapht.Graph;

/**
 * Generator which produces the
 * <a href="http://mathworld.wolfram.com/LineGraph.html">line graph</a> of a given input
 * graph.
 * The line graph of an undirected graph $G$ is another graph $L(G)$ that represents the
 * adjacencies between edges of $G$.
 * The line graph of a directed graph $G$ is the directed graph $L(G)$ whose vertex set
 * corresponds to the arc set of $G$ and having an arc directed from an edge $e_1$ to an
 * edge $e_2$ if in $G$, the head of $e_1$ meets the tail of $e_2$
 *
 * The runtime complexity is $O(|V|+|E|)$.
 *
 * <p>
 * More formally, let $G = (V, E)$ be a graph then its line graph $L(G)$ is such that
 * - each vertex of $L(G)$ represents an edge of $G$; and
 * - two vertices of $L(G)$ are adjacent if and only if their corresponding edges share
 *   a common endpoint ("are incident") in $G$.
 * <p>
 *
 * @author Nikhil Sharma
 * @since June 2018
 *
 *
 * @param <V> vertex type
 * @param <E1> edge type
 * @param <E2> edge type
 *
 */
public class LineGraphConstructor<V, E1, E2>
{

    private final Graph<V, E1> graph;

    /**
     * Line Graph Constructor
     *
     * @param graph input graph
     */
    public LineGraphConstructor(Graph<V, E1> graph)
    {
        this.graph = graph;
    }

    /**
     * Constructs line graph of a given graph.
     *
     * @param target target graph
     */
    public void constructGraph(Graph<E1, E2> target)
    {
        if (graph.getType().isDirected()) {

            for(V vertex : graph.vertexSet()){
                for(E1 edge1 : graph.incomingEdgesOf(vertex)){
                    for(E1 edge2 : graph.outgoingEdgesOf(vertex)){
                        target.addVertex(edge1);
                        target.addVertex(edge2);
                        target.addEdge(edge1, edge2);
                    }
                }
            }
        } else{ // undirected graph

            for(E1 edge : graph.edgeSet()){
                for(E1 edge1 : graph.incomingEdgesOf(graph.getEdgeTarget(edge))){
                    if(edge != edge1){
                        target.addVertex(edge);
                        target.addVertex(edge1);
                        target.addEdge(edge ,edge1);
                    }
                }

                for(E1 edge1 : graph.incomingEdgesOf(graph.getEdgeSource(edge))){
                    if(edge != edge1){
                        target.addVertex(edge);
                        target.addVertex(edge1);
                        target.addEdge(edge ,edge1);
                    }
                }
            }
        }
    }
}
