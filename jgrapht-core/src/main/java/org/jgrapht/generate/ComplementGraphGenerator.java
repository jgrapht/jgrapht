/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;

import java.util.*;

/**
 * Generator which produces the
 * <a href="http://mathworld.wolfram.com/GraphComplement.html">complement graph</a> of a given input
 * graph. The complement $\overline{G}$ of a graph $G$ consists of the same vertices as $G$, but
 * whose edge set consists of the edges not in $G$.
 * <p>
 * More formally, let $G = (V, E)$ be a graph and let $K$ consist of all 2-element subsets of $V$.
 * Then $\overline{G} = (V, K \setminus E)$ is the complement of $G$, where $K \setminus E$ is the
 * relative complement of $E$ in $K$. For directed graphs, the complement can be defined in the same
 * way, as a directed graph on the same vertex set, using the set of all 2-element ordered pairs of
 * $V$ in place of the set $K$ in the formula above.
 * <p>
 * The complement is not defined for multigraphs. If a multigraph is provided as input to this
 * generator, it will be treated as if it is a simple graph.
 *
 * @author Joris Kinable
 *
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class ComplementGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{

    private final Graph<V, E> graph;
    private final boolean generateSelfLoops;

    /**
     * Complement Graph Generator
     * 
     * @param graph input graph
     */
    public ComplementGraphGenerator(Graph<V, E> graph)
    {
        this(graph, false);
    }

    /**
     * Complement Graph Generator. If the target graph allows self-loops the complement of $G$ may
     * be defined by adding a self-loop to every vertex that does not have one in $G$. This behavior
     * can be controlled using the boolean <code>generateSelfLoops</code>.
     *
     * @param graph input graph
     * @param generateSelfLoops indicator whether self loops should be generated. If false, no
     *        self-loops are generated, independent of whether the target graph supports self-loops.
     */
    public ComplementGraphGenerator(Graph<V, E> graph, boolean generateSelfLoops)
    {
        this.graph = GraphTests.requireDirectedOrUndirected(graph);
        this.generateSelfLoops = generateSelfLoops;
    }

    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        Graphs.addAllVertices(target, graph.vertexSet());

        if (graph.getType().isDirected()) {
            for (V u : graph.vertexSet())
                for (V v : graph.vertexSet())
                    if (u == v)
                        continue;
                    else if (!graph.containsEdge(u, v))
                        target.addEdge(u, v);
        } else { // undirected graph
            List<V> vertices = new ArrayList<>(graph.vertexSet());
            for (int i = 0; i < vertices.size() - 1; i++) {
                for (int j = i + 1; j < vertices.size(); j++) {
                    V u = vertices.get(i);
                    V v = vertices.get(j);
                    if (!graph.containsEdge(u, v))
                        target.addEdge(u, v);
                }
            }
        }
        if (generateSelfLoops && target.getType().isAllowingSelfLoops()) {
            for (V v : graph.vertexSet()) {
                if (!graph.containsEdge(v, v))
                    target.addEdge(v, v);
            }
        }
    }
}
