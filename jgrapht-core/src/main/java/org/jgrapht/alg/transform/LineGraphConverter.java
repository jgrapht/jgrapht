/*
 * (C) Copyright 2018-2021, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.transform;

import org.jgrapht.*;

import java.util.*;
import java.util.function.*;

/**
 * Converter which produces the <a href="http://mathworld.wolfram.com/LineGraph.html">line graph</a>
 * of a given input graph. The line graph of an undirected graph $G$ is another graph $L(G)$ that
 * represents the adjacencies between edges of $G$. The line graph of a directed graph $G$ is the
 * directed graph $L(G)$ whose vertex set corresponds to the arc set of $G$ and having an arc
 * directed from an edge $e_1$ to an edge $e_2$ if in $G$, the head of $e_1$ meets the tail of $e_2$
 *
 * <p>
 * More formally, let $G = (V, E)$ be a graph then its line graph $L(G)$ is such that
 * <ul>
 * <li>Each vertex of $L(G)$ represents an edge of $G$</li>
 * <li>If $G$ is undirected: two vertices of $L(G)$ are adjacent if and only if their corresponding
 * edges share a common endpoint ("are incident") in $G$</li>
 * <li>If $G$ is directed: two vertices of $L(G)$ corresponding to respectively arcs $(u,v)$ and
 * $(r,s)$ in $G$ are adjacent if and only if $v=r$.</li>
 * </ul>
 * <p>
 *
 * @author Joris Kinable
 * @author Nikhil Sharma
 *
 *
 * @param <V> vertex type of input graph
 * @param <E> edge type of input graph
 * @param <EE> edge type of target graph
 *
 */
public class LineGraphConverter<V, E, EE>
{

    private final Graph<V, E> graph;

    /**
     * Line Graph Converter
     *
     * @param graph graph to be converted. This implementation supports multigraphs and
     *        pseudographs.
     */
    public LineGraphConverter(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
    }

    /**
     * Constructs a line graph $L(G)$ of the input graph $G(V,E)$. If the input graph is directed,
     * the result is a line digraph. The result is stored in the target graph.
     *
     * @param target target graph
     */
    public void convertToLineGraph(final Graph<E, EE> target)
    {
        this.convertToLineGraph(target, null);
    }

    /**
     * Constructs a line graph of the input graph. If the input graph is directed, the result is a
     * line digraph. The result is stored in the target graph. A weight function is provided to set
     * edge weights of the line graph edges. Notice that the target graph must be a weighted graph
     * for this to work. Recall that in a line graph $L(G)$ of a graph $G(V,E)$ there exists an edge
     * $e$ between $e_1\in E$ and $e_2\in E$ if the head of $e_1$ is incident to the tail of $e_2$.
     * To determine the weight of $e$ in $L(G)$, the weight function takes as input $e_1$ and $e_2$.
     *
     * <p>
     * Note: a special case arises when graph $G$ contains self-loops. Self-loops (as well as
     * multiple edges) simply add additional nodes to line graph $L(G)$. When $G$ is
     * <em>directed</em>, a self-loop $e=(v,v)$ in $G$ results in a vertex $e$ in $L(G)$, and in
     * addition a self-loop $(e,e)$ in $L(G)$, since, by definition, the head of $e$ in $G$ is
     * incident to its own tail. When $G$ is <em>undirected</em>, a self-loop $e=(v,v)$ in $G$
     * results in a vertex $e$ in $L(G)$, but <em>no</em> self-loop $(e,e)$ is added to $L(G)$,
     * since, by convention, the line graph of an undirected graph is commonly assumed to be a
     * simple graph.
     *
     * @param target target graph
     * @param weightFunction weight function
     */
    public void convertToLineGraph(
        final Graph<E, EE> target, final BiFunction<E, E, Double> weightFunction)
    {
        Graphs.addAllVertices(target, graph.edgeSet());
        if (graph.getType().isDirected()) {
            for (V vertex : graph.vertexSet()) {
                for (E e1 : graph.incomingEdgesOf(vertex)) {
                    for (E e2 : graph.outgoingEdgesOf(vertex)) {
                        EE edge = target.addEdge(e1, e2);
                        if (weightFunction != null)
                            target.setEdgeWeight(edge, weightFunction.apply(e1, e2));
                    }
                }
            }
        } else { // undirected graph
            for (V v : graph.vertexSet()) {
                for (E e1 : graph.edgesOf(v)) {
                    for (E e2 : graph.edgesOf(v)) {
                        if (e1 != e2) {
                            EE edge = target.addEdge(e1, e2);
                            if (weightFunction != null)
                                target.setEdgeWeight(edge, weightFunction.apply(e1, e2));
                        }
                    }
                }
            }

        }
    }
}
